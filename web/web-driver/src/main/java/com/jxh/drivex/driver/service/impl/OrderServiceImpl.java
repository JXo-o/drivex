package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.driver.service.OrderService;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderInfoFeignClient orderInfoFeignClient;

    public OrderServiceImpl(OrderInfoFeignClient orderInfoFeignClient) {
        this.orderInfoFeignClient = orderInfoFeignClient;
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }
}
