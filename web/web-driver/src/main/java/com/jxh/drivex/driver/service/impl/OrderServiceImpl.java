package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.dispatch.client.NewOrderFeignClient;
import com.jxh.drivex.driver.service.OrderService;
import com.jxh.drivex.map.client.MapFeignClient;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.form.order.StartDriveForm;
import com.jxh.drivex.model.form.order.UpdateOrderCartForm;
import com.jxh.drivex.model.vo.map.DrivingLineVo;
import com.jxh.drivex.model.vo.order.CurrentOrderInfoVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import com.jxh.drivex.model.vo.order.OrderInfoVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderInfoFeignClient orderInfoFeignClient;
    private final NewOrderFeignClient newOrderFeignClient;
    private final MapFeignClient mapFeignClient;

    public OrderServiceImpl(
            OrderInfoFeignClient orderInfoFeignClient,
            NewOrderFeignClient newOrderFeignClient,
            MapFeignClient mapFeignClient
    ) {
        this.orderInfoFeignClient = orderInfoFeignClient;
        this.newOrderFeignClient = newOrderFeignClient;
        this.mapFeignClient = mapFeignClient;
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }

    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        return newOrderFeignClient.findNewOrderQueueData(driverId).getData();
    }

    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        return orderInfoFeignClient.robNewOrder(driverId, orderId).getData();
    }

    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        return orderInfoFeignClient.searchDriverCurrentOrder(driverId).getData();
    }

    @Override
    public OrderInfoVo getOrderInfo(Long orderId, Long driverId) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if(!orderInfo.getDriverId().equals(driverId)) {
            throw new DrivexException(ResultCodeEnum.ORDER_ID_NOT_FOUND);
        }
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setOrderId(orderId);
        BeanUtils.copyProperties(orderInfo, orderInfoVo);
        return orderInfoVo;
    }

    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        return mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
    }

    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        return orderInfoFeignClient.driverArriveStartLocation(orderId, driverId).getData();
    }

    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        return orderInfoFeignClient.updateOrderCart(updateOrderCartForm).getData();
    }

    @Override
    public Boolean startDrive(StartDriveForm startDriveForm) {
        return orderInfoFeignClient.startDrive(startDriveForm).getData();
    }
}
