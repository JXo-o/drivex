package com.jxh.drivex.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.form.order.OrderInfoForm;
import com.jxh.drivex.model.form.order.StartDriveForm;
import com.jxh.drivex.model.form.order.UpdateOrderBillForm;
import com.jxh.drivex.model.form.order.UpdateOrderCartForm;
import com.jxh.drivex.model.vo.order.CurrentOrderInfoVo;

public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrderInfo(OrderInfoForm orderInfoForm);

    Integer getOrderStatus(Long orderId);

    Boolean robNewOrder(Long driverId, Long orderId);

    CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId);

    CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId);

    Boolean driverArriveStartLocation(Long orderId, Long driverId);

    Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm);

    Boolean startDrive(StartDriveForm startDriveForm);

    Long getOrderNumByTime(String startTime, String endTime);

    Boolean endDrive(UpdateOrderBillForm updateOrderBillForm);
}
