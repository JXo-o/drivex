package com.jxh.drivex.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.entity.order.OrderStatusLog;
import com.jxh.drivex.model.enums.OrderStatus;
import com.jxh.drivex.model.form.order.OrderInfoForm;
import com.jxh.drivex.order.mapper.OrderInfoMapper;
import com.jxh.drivex.order.mapper.OrderStatusLogMapper;
import com.jxh.drivex.order.service.OrderInfoService;
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

    public OrderInfoServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            OrderStatusLogMapper orderStatusLogMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.orderStatusLogMapper = orderStatusLogMapper;
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
