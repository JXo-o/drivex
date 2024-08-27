package com.jxh.drivex.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.entity.order.OrderStatusLog;
import com.jxh.drivex.model.enums.OrderStatus;
import com.jxh.drivex.model.form.order.OrderInfoForm;
import com.jxh.drivex.order.mapper.OrderInfoMapper;
import com.jxh.drivex.order.mapper.OrderStatusLogMapper;
import com.jxh.drivex.order.service.OrderInfoService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    private final RedisTemplate<String, String> redisTemplate;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final RedissonClient redissonClient;

    public OrderInfoServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            OrderStatusLogMapper orderStatusLogMapper,
            OrderInfoMapper orderInfoMapper,
            RedissonClient redissonClient
    ) {
        this.redisTemplate = redisTemplate;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.redissonClient = redissonClient;
    }

    /**
     * 保存订单信息。
     * <p>
     * 此方法根据传入的订单信息表单对象创建一个新的订单记录，并设置订单状态为等待接单。
     * 生成唯一的订单号，并在数据库中保存订单信息。此外，还会在 Redis 中存储订单接单标记，
     * 并且会记录订单状态日志。
     * </p>
     *
     * @param orderInfoForm 包含订单详细信息的表单对象
     * @return 返回新创建的订单的唯一ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfoForm orderInfoForm) {
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm, orderInfo);
        String orderNo = UUID.randomUUID().toString().replaceAll("-","");
        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());
        orderInfo.setOrderNo(orderNo);
        this.save(orderInfo);
        this.log(orderInfo.getId(), orderInfo.getStatus());
        redisTemplate.opsForValue().set(
                RedisConstant.ORDER_ACCEPT_MARK + orderInfo.getId(),
                "0",
                RedisConstant.ORDER_ACCEPT_MARK_EXPIRES_TIME,
                TimeUnit.MINUTES
        );
        // TODO: 生成订单之后，发送延迟消息
        return orderInfo.getId();
    }

    /**
     * 根据订单ID获取订单状态。
     * <p>
     * 此方法根据传入的订单ID查询订单状态。如果订单不存在，则返回表示无此订单的状态码。
     * </p>
     *
     * @param orderId 订单的唯一ID
     * @return 返回订单的状态码
     */
    @Override
    public Integer getOrderStatus(Long orderId) {
        OrderInfo orderInfo = this.lambdaQuery()
                .eq(OrderInfo::getId, orderId)
                .select(OrderInfo::getStatus)
                .one();
        if(orderInfo == null) {
            return OrderStatus.NULL_ORDER.getStatus();
        }
        return orderInfo.getStatus();
    }

    /**
     * 尝试让司机抢新订单。
     *
     * <ol>
     *   <li>首先检查 Redis 中是否存在 {@code ORDER_ACCEPT_MARK} 键，以确定订单是否还可以被抢。</li>
     *   <li>如果该键不存在，抛出 {@code DrivexException} 异常，表示抢单失败。</li>
     *   <li>使用 Redisson 初始化一个分布式锁，锁的键为 {@code ROB_NEW_ORDER_LOCK} 加上订单 ID。</li>
     *   <li>尝试使用非阻塞方式获取锁，指定等待时间和加锁时间。</li>
     *   <li>如果获取到锁：</li>
     *   <ol>
     *     <li>双重检查锁：再次检查 {@code ORDER_ACCEPT_MARK} 键是否存在，以防止重复抢单。</li>
     *     <li>如果键不存在，抛出 {@code DrivexException} 异常，表示抢单失败。</li>
     *     <li>更新订单状态，将订单状态改为已接受，并记录司机 ID 和接受时间。</li>
     *     <li>如果更新订单失败，抛出 {@code DrivexException} 异常，表示抢单失败。</li>
     *     <li>记录日志，记录订单 ID 和新的订单状态。</li>
     *     <li>删除 Redis 中的订单标识 {@code ORDER_ACCEPT_MARK}。</li>
     *   </ol>
     *   <li>如果发生 {@code InterruptedException} 异常，抛出 {@code DrivexException} 异常，表示抢单失败。</li>
     *   <li>在 `finally` 块中，如果锁仍然被持有，释放锁。</li>
     * </ol>
     *
     * @param driverId 司机的 ID
     * @param orderId 订单的 ID
     * @return {@code true} 表示抢单成功
     * @throws DrivexException 如果抢单失败时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean robNewOrder(Long driverId, Long orderId) {
        if(Boolean.FALSE.equals(redisTemplate.hasKey(RedisConstant.ORDER_ACCEPT_MARK))) {
            throw new DrivexException(ResultCodeEnum.ORDER_CREATION_FAILURE);
        }
        RLock lock = redissonClient.getLock(RedisConstant.ROB_NEW_ORDER_LOCK + orderId);
        try {
            boolean flag = lock.tryLock(
                    RedisConstant.ROB_NEW_ORDER_LOCK_WAIT_TIME,
                    RedisConstant.ROB_NEW_ORDER_LOCK_LEASE_TIME,
                    TimeUnit.SECONDS
            );
            if (flag) {
                if(Boolean.FALSE.equals(redisTemplate.hasKey(RedisConstant.ORDER_ACCEPT_MARK))) {
                    throw new DrivexException(ResultCodeEnum.ORDER_CREATION_FAILURE);
                }
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setId(orderId);
                orderInfo.setStatus(OrderStatus.ACCEPTED.getStatus());
                orderInfo.setAcceptTime(new Date());
                orderInfo.setDriverId(driverId);
                int rows = orderInfoMapper.updateById(orderInfo);
                if(rows != 1) {
                    throw new DrivexException(ResultCodeEnum.ORDER_CREATION_FAILURE);
                }
                this.log(orderId, orderInfo.getStatus());
                redisTemplate.delete(RedisConstant.ORDER_ACCEPT_MARK);
            }
        } catch (InterruptedException e) {
            throw new DrivexException(ResultCodeEnum.ORDER_CREATION_FAILURE);
        } finally {
            if(lock.isLocked()) {
                lock.unlock();
            }
        }
        return true;
    }

    /**
     * 记录订单状态日志。
     * <p>
     * 此方法用于记录订单状态变化的日志信息，包括订单ID、状态、操作时间等信息，并将其插入到订单状态日志表中。
     * </p>
     *
     * @param orderId 订单的唯一ID
     * @param status 当前订单的状态码
     */
    private void log(Long orderId, Integer status) {
        OrderStatusLog orderStatusLog = new OrderStatusLog();
        orderStatusLog.setOrderId(orderId);
        orderStatusLog.setOrderStatus(status);
        orderStatusLog.setOperateTime(new Date());
        orderStatusLogMapper.insert(orderStatusLog);
    }
}
