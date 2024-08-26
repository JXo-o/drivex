package com.jxh.drivex.driver.service;

import com.jxh.drivex.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface OrderService {

    Integer getOrderStatus(Long orderId);

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);
}
