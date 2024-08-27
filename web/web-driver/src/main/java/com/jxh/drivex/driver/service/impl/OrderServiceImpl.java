package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.dispatch.client.NewOrderFeignClient;
import com.jxh.drivex.driver.service.OrderService;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderInfoFeignClient orderInfoFeignClient;
    private final NewOrderFeignClient newOrderFeignClient;

    public OrderServiceImpl(
            OrderInfoFeignClient orderInfoFeignClient,
            NewOrderFeignClient newOrderFeignClient
    ) {
        this.orderInfoFeignClient = orderInfoFeignClient;
        this.newOrderFeignClient = newOrderFeignClient;
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
}
