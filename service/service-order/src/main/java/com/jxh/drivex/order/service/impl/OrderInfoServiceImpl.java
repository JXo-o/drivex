package com.jxh.drivex.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.common.constant.MqConst;
import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.common.constant.SystemConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.common.service.RabbitService;
import com.jxh.drivex.model.entity.order.*;
import com.jxh.drivex.model.enums.OrderStatus;
import com.jxh.drivex.model.form.order.OrderInfoForm;
import com.jxh.drivex.model.form.order.StartDriveForm;
import com.jxh.drivex.model.form.order.UpdateOrderBillForm;
import com.jxh.drivex.model.form.order.UpdateOrderCartForm;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.order.*;
import com.jxh.drivex.order.mapper.OrderBillMapper;
import com.jxh.drivex.order.mapper.OrderInfoMapper;
import com.jxh.drivex.order.mapper.OrderProfitsharingMapper;
import com.jxh.drivex.order.mapper.OrderStatusLogMapper;
import com.jxh.drivex.order.service.OrderInfoService;
import com.jxh.drivex.order.service.OrderMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    private final RedisTemplate<String, String> redisTemplate;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final RedissonClient redissonClient;
    private final OrderMonitorService orderMonitorService;
    private final OrderBillMapper orderBillMapper;
    private final OrderProfitsharingMapper orderProfitsharingMapper;
    private final RabbitService rabbitService;

    public OrderInfoServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            OrderStatusLogMapper orderStatusLogMapper,
            OrderInfoMapper orderInfoMapper,
            RedissonClient redissonClient,
            OrderMonitorService orderMonitorService,
            OrderBillMapper orderBillMapper,
            OrderProfitsharingMapper orderProfitsharingMapper,
            RabbitService rabbitService
    ) {
        this.redisTemplate = redisTemplate;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.redissonClient = redissonClient;
        this.orderMonitorService = orderMonitorService;
        this.orderBillMapper = orderBillMapper;
        this.orderProfitsharingMapper = orderProfitsharingMapper;
        this.rabbitService = rabbitService;
    }

    /**
     * 保存订单信息。
     * <p>
     * 此方法根据传入的订单信息表单对象创建一个新的订单记录，并设置订单状态为等待接单。
     * 生成唯一的订单号，并在数据库中保存订单信息。此外，还会在 Redis 中存储订单接单标记，
     * 并且会记录订单状态日志。最后，会发送延迟消息，用于取消订单。
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
        rabbitService.sendDelayMessage(
                MqConst.EXCHANGE_CANCEL_ORDER,
                MqConst.ROUTING_CANCEL_ORDER,
                String.valueOf(orderInfo.getId()),
                SystemConstant.CANCEL_ORDER_DELAY_TIME
        );
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
     * 乘客端查找当前订单。
     * <p>
     * 此方法根据乘客的 ID 查询当前订单信息。查询条件包括订单状态为已接单、司机已到达、更新代驾车辆信息、开始服务、结束服务和待付款的订单。
     * 查询结果按订单 ID 降序排列，并限制只返回一条记录。
     * </p>
     *
     * @param customerId 乘客的 ID
     * @return 返回包含当前订单信息的 CurrentOrderInfoVo 对象。如果没有当前订单，则返回的对象中 isHasCurrentOrder 字段为 false。
     */
    @Override
    public CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId) {
        Integer[] statusArray = {
                OrderStatus.ACCEPTED.getStatus(),
                OrderStatus.DRIVER_ARRIVED.getStatus(),
                OrderStatus.UPDATE_CART_INFO.getStatus(),
                OrderStatus.START_SERVICE.getStatus(),
                OrderStatus.END_SERVICE.getStatus(),
                OrderStatus.UNPAID.getStatus()
        };
        return this.searchCurrentOrder(customerId, OrderInfo::getCustomerId, statusArray);
    }

    /**
     * 司机端查找当前订单。
     * <p>
     * 此方法根据乘客的 ID 查询当前订单信息。查询条件包括订单状态为已接单、司机已到达、更新代驾车辆信息、开始服务、结束服务的订单。
     * 查询结果按订单 ID 降序排列，并限制只返回一条记录。
     * </p>
     *
     * @param driverId 司机的 ID
     * @return 返回包含当前订单信息的 CurrentOrderInfoVo 对象。如果没有当前订单，则返回的对象中 isHasCurrentOrder 字段为 false。
     */
    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        Integer[] statusArray = {
                OrderStatus.ACCEPTED.getStatus(),
                OrderStatus.DRIVER_ARRIVED.getStatus(),
                OrderStatus.UPDATE_CART_INFO.getStatus(),
                OrderStatus.START_SERVICE.getStatus(),
                OrderStatus.END_SERVICE.getStatus()
        };
        return this.searchCurrentOrder(driverId, OrderInfo::getDriverId, statusArray);
    }

    /**
     * 司机到达起始点并更新订单信息。
     *
     * @param orderId 订单ID
     * @param driverId 司机ID
     * @return 更新订单是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        LambdaUpdateWrapper<OrderInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getDriverId, driverId)
                .set(OrderInfo::getStatus, OrderStatus.DRIVER_ARRIVED.getStatus())
                .set(OrderInfo::getArriveTime, new Date());
        if(orderInfoMapper.update( updateWrapper) == 1) {
            this.log(orderId, OrderStatus.DRIVER_ARRIVED.getStatus());
        } else {
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }

    /**
     * 更新代驾车辆信息。
     *
     * @param updateOrderCartForm 更新代驾车辆信息表单
     * @return 更新是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, updateOrderCartForm.getOrderId());
        queryWrapper.eq(OrderInfo::getDriverId, updateOrderCartForm.getDriverId());
        OrderInfo updateOrderInfo = new OrderInfo();
        BeanUtils.copyProperties(updateOrderCartForm, updateOrderInfo);
        updateOrderInfo.setStatus(OrderStatus.UPDATE_CART_INFO.getStatus());
        if(orderInfoMapper.update(updateOrderInfo, queryWrapper) == 1) {
            this.log(updateOrderCartForm.getOrderId(), OrderStatus.UPDATE_CART_INFO.getStatus());
        } else {
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }

    /**
     * 司机开始代驾服务，并更新订单信息。
     *
     * @param startDriveForm 开始代驾服务表单
     * @return 更新是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startDrive(StartDriveForm startDriveForm) {
        LambdaUpdateWrapper<OrderInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfo::getId, startDriveForm.getOrderId())
                .eq(OrderInfo::getDriverId, startDriveForm.getDriverId())
                .set(OrderInfo::getStatus, OrderStatus.START_SERVICE.getStatus())
                .set(OrderInfo::getStartServiceTime, new Date());
        if(orderInfoMapper.update(updateWrapper) == 1) {
            this.log(startDriveForm.getOrderId(), OrderStatus.START_SERVICE.getStatus());
        } else {
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }
        OrderMonitor orderMonitor = new OrderMonitor();
        orderMonitor.setOrderId(startDriveForm.getOrderId());
        orderMonitorService.saveOrderMonitor(orderMonitor);
        return true;
    }

    /**
     * 根据时间段获取订单数。
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单数
     */
    @Override
    public Long getOrderNumByTime(String startTime, String endTime) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(OrderInfo::getStartServiceTime, startTime);
        queryWrapper.lt(OrderInfo::getStartServiceTime, endTime);
        return orderInfoMapper.selectCount(queryWrapper);
    }

    /**
     * 结束代驾服务并更新订单账单。
     * <p>
     * 此方法根据传入的更新订单账单表单对象更新订单信息，并将订单状态改为已结束服务。
     * 如果更新订单信息成功，则将订单账单信息插入到订单账单表中，并将订单分账信息插入到订单分账表中。
     * 如果更新订单信息失败，则抛出 {@code DrivexException} 异常。
     * </p>
     *
     * @param updateOrderBillForm 更新订单账单表单对象
     * @return 更新是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean endDrive(UpdateOrderBillForm updateOrderBillForm) {
        LambdaUpdateWrapper<OrderInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfo::getId, updateOrderBillForm.getOrderId())
                .eq(OrderInfo::getDriverId, updateOrderBillForm.getDriverId())
                .set(OrderInfo::getStatus, OrderStatus.END_SERVICE.getStatus())
                .set(OrderInfo::getRealAmount, updateOrderBillForm.getTotalAmount())
                .set(OrderInfo::getFavourFee, updateOrderBillForm.getFavourFee())
                .set(OrderInfo::getEndServiceTime, new Date())
                .set(OrderInfo::getRealDistance, updateOrderBillForm.getRealDistance());

        if(orderInfoMapper.update(updateWrapper) == 1) {
            this.log(updateOrderBillForm.getOrderId(), OrderStatus.END_SERVICE.getStatus());
            OrderBill orderBill = new OrderBill();
            BeanUtils.copyProperties(updateOrderBillForm, orderBill);
            orderBill.setOrderId(updateOrderBillForm.getOrderId());
            orderBill.setPayAmount(orderBill.getTotalAmount());
            orderBillMapper.insert(orderBill);

            OrderProfitsharing orderProfitsharing = new OrderProfitsharing();
            BeanUtils.copyProperties(updateOrderBillForm, orderProfitsharing);
            orderProfitsharing.setOrderId(updateOrderBillForm.getOrderId());
            orderProfitsharing.setRuleId(updateOrderBillForm.getProfitsharingRuleId());
            orderProfitsharing.setStatus(1);
            orderProfitsharingMapper.insert(orderProfitsharing);
        } else {
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }

    /**
     * 系统取消订单。
     *
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void systemCancelOrder(long orderId) {
        Integer orderStatus = this.getOrderStatus(orderId);
        if(orderStatus != null && orderStatus.equals(OrderStatus.WAITING_ACCEPT.getStatus())) {
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setId(orderId);
            if(orderInfoMapper.updateById(orderInfo) == 1) {
                this.log(orderInfo.getId(), orderInfo.getStatus());
                redisTemplate.delete(RedisConstant.ORDER_ACCEPT_MARK + orderId);
            } else {
                throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
            }
        }
    }

    /**
     * 分页查询乘客订单列表。
     *
     * @param pageParam 分页参数
     * @param customerId 乘客ID
     * @return 订单列表
     */
    @Override
    public PageVo<OrderListVo> findCustomerOrderPage(Page<OrderInfo> pageParam, Long customerId) {
        Page<OrderListVo> pageInfo = orderInfoMapper.selectCustomerOrderPage(pageParam, customerId);
        return new PageVo<>(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    /**
     * 分页查询司机订单列表。
     *
     * @param pageParam 分页参数
     * @param driverId 司机ID
     * @return 订单列表
     */
    @Override
    public PageVo<OrderListVo> findDriverOrderPage(Page<OrderInfo> pageParam, Long driverId) {
        Page<OrderListVo> pageInfo = orderInfoMapper.selectDriverOrderPage(pageParam, driverId);
        return new PageVo<>(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    /**
     * 根据订单ID获取实际账单信息。
     *
     * @param orderId 订单ID
     * @return 实际账单信息
     */
    @Override
    public OrderBillVo getOrderBillInfo(Long orderId) {
        OrderBill orderBill = orderBillMapper.selectOne(
            new LambdaQueryWrapper<OrderBill>().eq(OrderBill::getOrderId, orderId)
        );
        OrderBillVo orderBillVo = new OrderBillVo();
        BeanUtils.copyProperties(orderBill, orderBillVo);
        return orderBillVo;
    }

    /**
     * 根据订单ID获取实际分账信息。
     *
     * @param orderId 订单ID
     * @return 实际分账信息
     */
    @Override
    public OrderProfitsharingVo getOrderProfitsharing(Long orderId) {
        OrderProfitsharing orderProfitsharing = orderProfitsharingMapper.selectOne(
                new LambdaQueryWrapper<OrderProfitsharing>().eq(OrderProfitsharing::getOrderId, orderId)
        );
        OrderProfitsharingVo orderProfitsharingVo = new OrderProfitsharingVo();
        BeanUtils.copyProperties(orderProfitsharing, orderProfitsharingVo);
        return orderProfitsharingVo;
    }

    /**
     * 司机更新订单为待付款，即发送账单信息。
     *
     * @param orderId 订单ID
     * @param driverId 司机ID
     * @return 发送是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendOrderBillInfo(Long orderId, Long driverId) {
        LambdaUpdateWrapper<OrderInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getDriverId, driverId)
                .set(OrderInfo::getStatus, OrderStatus.UNPAID.getStatus());
        if(orderInfoMapper.update(updateWrapper) == 1) {
            this.log(orderId, OrderStatus.UNPAID.getStatus());
        } else {
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }

    /**
     * 获取订单支付信息。
     *
     * @param orderNo 订单号
     * @param customerId 乘客ID
     * @return 订单支付信息
     */
    @Override
    public OrderPayVo getOrderPayVo(String orderNo, Long customerId) {
        OrderPayVo orderPayVo = orderInfoMapper.selectOrderPayVo(orderNo, customerId);
        if(orderPayVo != null) {
            String content = orderPayVo.getStartLocation() + " 到 " + orderPayVo.getEndLocation();
            orderPayVo.setContent(content);
        }
        return orderPayVo;
    }

    /**
     * 更新订单支付状态。
     *
     * @param orderNo 订单号
     * @return 更新是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOrderPayStatus(String orderNo) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getOrderNo, orderNo);
        queryWrapper.select(
                OrderInfo::getId,
                OrderInfo::getDriverId,
                OrderInfo::getStatus
        );
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        if(orderInfo == null || orderInfo.getStatus().equals(OrderStatus.PAID.getStatus()))
            return true;

        LambdaUpdateWrapper<OrderInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderInfo::getOrderNo, orderNo)
                .set(OrderInfo::getStatus, OrderStatus.PAID.getStatus())
                .set(OrderInfo::getPayTime, new Date());
        if(orderInfoMapper.update(updateWrapper) == 1) {
            this.log(orderInfo.getId(), OrderStatus.PAID.getStatus());
        } else {
            log.error("订单支付回调更新订单状态失败，订单号为：" + orderNo);
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }

    /**
     * 获取订单奖励费用。
     *
     * @param orderNo 订单号
     * @return 订单奖励费用
     */
    @Override
    public OrderRewardVo getOrderRewardFee(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderNo, orderNo)
                        .select(OrderInfo::getId,OrderInfo::getDriverId)
        );
        OrderBill orderBill = orderBillMapper.selectOne(
                new LambdaQueryWrapper<OrderBill>()
                        .eq(OrderBill::getOrderId, orderInfo.getId())
                        .select(OrderBill::getRewardFee)
        );
        OrderRewardVo orderRewardVo = new OrderRewardVo();
        orderRewardVo.setOrderId(orderInfo.getId());
        orderRewardVo.setDriverId(orderInfo.getDriverId());
        orderRewardVo.setRewardFee(orderBill.getRewardFee());
        return orderRewardVo;
    }

    /**
     * 更新订单分账状态。
     *
     * @param orderNo 订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfitsharingStatus(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderNo, orderNo)
                        .select(OrderInfo::getId)
        );
        LambdaUpdateWrapper<OrderProfitsharing> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderProfitsharing::getOrderId, orderInfo.getId())
                .set(OrderProfitsharing::getStatus, 2);
        if (orderProfitsharingMapper.update(updateWrapper) == 1) {
            log.info("订单分账状态更新成功，订单号为：" + orderNo);
        } else {
            log.error("订单分账状态更新失败，订单号为：" + orderNo);
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
        }

    }

    /**
     * 更新订单优惠券金额。
     *
     * @param orderId 订单ID
     * @param couponAmount 优惠券金额
     * @return 更新是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCouponAmount(Long orderId, BigDecimal couponAmount) {
        if(orderBillMapper.updateCouponAmount(orderId, couponAmount) != 1) {
            throw new DrivexException(ResultCodeEnum.UPDATE_ERROR);
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

    /**
     * 根据 ID 查询当前订单。
     * <p>
     * 此方法根据传入的 ID 和 ID 获取函数查询当前订单信息。查询条件包括订单状态在指定的状态数组中的订单。
     * 查询结果按订单 ID 降序排列，并限制只返回一条记录。
     * </p>
     *
     * @param id 订单相关的 ID（如客户 ID 或司机 ID）
     * @param idGetter 获取订单相关 ID 的函数（如 OrderInfo::getCustomerId 或 OrderInfo::getDriverId）
     * @param statusArray 包含需要查询的订单状态的数组
     * @return 返回包含当前订单信息的 CurrentOrderInfoVo 对象。如果没有找到符合条件的订单，则 isHasCurrentOrder 字段为 false。
     */
    private CurrentOrderInfoVo searchCurrentOrder(Long id, SFunction<OrderInfo, Long> idGetter, Integer[] statusArray) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(idGetter, id);
        queryWrapper.in(OrderInfo::getStatus, (Object) statusArray);
        queryWrapper.orderByDesc(OrderInfo::getId);
        queryWrapper.last("limit 1");
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        if (orderInfo != null) {
            currentOrderInfoVo.setStatus(orderInfo.getStatus());
            currentOrderInfoVo.setOrderId(orderInfo.getId());
            currentOrderInfoVo.setIsHasCurrentOrder(true);
        } else {
            currentOrderInfoVo.setIsHasCurrentOrder(false);
        }
        return currentOrderInfoVo;
    }
}
