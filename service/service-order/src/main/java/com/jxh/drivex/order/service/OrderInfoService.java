package com.jxh.drivex.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.form.order.OrderInfoForm;

public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrderInfo(OrderInfoForm orderInfoForm);

    Integer getOrderStatus(Long orderId);

    Boolean robNewOrder(Long driverId, Long orderId);
}
