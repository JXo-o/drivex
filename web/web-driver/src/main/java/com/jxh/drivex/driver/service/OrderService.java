package com.jxh.drivex.driver.service;

import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.form.order.StartDriveForm;
import com.jxh.drivex.model.form.order.UpdateOrderCartForm;
import com.jxh.drivex.model.vo.map.DrivingLineVo;
import com.jxh.drivex.model.vo.order.CurrentOrderInfoVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import com.jxh.drivex.model.vo.order.OrderInfoVo;

import java.util.List;

public interface OrderService {

    Integer getOrderStatus(Long orderId);

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);

    Boolean robNewOrder(Long driverId, Long orderId);

    CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId);

    OrderInfoVo getOrderInfo(Long orderId, Long driverId);

    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);

    Boolean driverArriveStartLocation(Long orderId, Long driverId);

    Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm);

    Boolean startDrive(StartDriveForm startDriveForm);
}
