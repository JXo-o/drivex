package com.jxh.drivex.order.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.form.order.OrderInfoForm;
import com.jxh.drivex.model.form.order.StartDriveForm;
import com.jxh.drivex.model.form.order.UpdateOrderBillForm;
import com.jxh.drivex.model.form.order.UpdateOrderCartForm;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.order.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "订单API接口管理")
@RestController
@RequestMapping(value="/order/info")
public class OrderInfoController {

    @Operation(summary = "保存订单信息")
    @PostMapping("/saveOrderInfo")
    Result<Long> saveOrderInfo(@RequestBody OrderInfoForm orderInfoForm) {
        return Result.ok();
    }

    @Operation(summary = "根据订单id获取订单状态")
    @GetMapping("/getOrderStatus/{orderId}")
    Result<Integer> getOrderStatus(@PathVariable("orderId") Long orderId) {
        return Result.ok();
    }

    @Operation(summary = "司机抢单")
    @GetMapping("/robNewOrder/{driverId}/{orderId}")
    Result<Boolean> robNewOrder(
            @PathVariable("driverId") Long driverId,
            @PathVariable("orderId") Long orderId
    ) {
        return Result.ok();
    }

    @Operation(summary = "乘客端查找当前订单")
    @GetMapping("/searchCustomerCurrentOrder/{customerId}")
    Result<CurrentOrderInfoVo> searchCustomerCurrentOrder(@PathVariable("customerId") Long customerId) {
        return Result.ok();
    }

    @Operation(summary = "司机端查找当前订单")
    @GetMapping("/searchDriverCurrentOrder/{driverId}")
    Result<CurrentOrderInfoVo> searchDriverCurrentOrder(@PathVariable("driverId") Long driverId) {
        return Result.ok();
    }

    @Operation(summary = "根据订单id获取订单信息")
    @GetMapping("/getOrderInfo/{orderId}")
    Result<OrderInfo> getOrderInfo(@PathVariable("orderId") Long orderId) {
        return Result.ok();
    }

    @Operation(summary = "司机到达起始点")
    @GetMapping("/driverArriveStartLocation/{orderId}/{driverId}")
    Result<Boolean> driverArriveStartLocation(
            @PathVariable("orderId") Long orderId,
            @PathVariable("driverId") Long driverId
    ) {
        return Result.ok();
    }

    @Operation(summary = "更新代驾车辆信息")
    @PostMapping("/updateOrderCart")
    Result<Boolean> updateOrderCart(@RequestBody UpdateOrderCartForm updateOrderCartForm) {
        return Result.ok();
    }

    @Operation(summary = "开始代驾服务")
    @PostMapping("/startDrive")
    Result<Boolean> startDrive(@RequestBody StartDriveForm startDriveForm) {
        return Result.ok();
    }

    @Operation(summary = "根据时间段获取订单数")
    @GetMapping("/getOrderNumByTime/{startTime}/{endTime}")
    Result<Long> getOrderNumByTime(
            @PathVariable("startTime") String startTime,
            @PathVariable("endTime") String endTime
    ) {
        return Result.ok();
    }

    @Operation(summary = "结束代驾服务更新订单账单")
    @PostMapping("/endDrive")
    Result<Boolean> endDrive(@RequestBody UpdateOrderBillForm updateOrderBillForm) {
        return Result.ok();
    }

    @Operation(summary = "获取乘客订单分页列表")
    @GetMapping("/findCustomerOrderPage/{customerId}/{page}/{limit}")
    Result<PageVo<OrderInfo>> findCustomerOrderPage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    ) {
        return Result.ok();
    }

    @Operation(summary = "获取司机订单分页列表")
    @GetMapping("/findDriverOrderPage/{driverId}/{page}/{limit}")
    Result<PageVo<OrderInfo>> findDriverOrderPage(
            @PathVariable("driverId") Long driverId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    ) {
        return Result.ok();
    }

    @Operation(summary = "根据订单id获取实际账单信息")
    @GetMapping("/getOrderBillInfo/{orderId}")
    Result<OrderBillVo> getOrderBillInfo(@PathVariable("orderId") Long orderId) {
        return Result.ok();
    }

    @Operation(summary = "根据订单id获取实际分账信息")
    @GetMapping("/getOrderProfitsharing/{orderId}")
    Result<OrderProfitsharingVo> getOrderProfitsharing(@PathVariable("orderId") Long orderId) {
        return Result.ok();
    }

    @Operation(summary = "司机发送账单信息")
    @GetMapping("/sendOrderBillInfo/{orderId}/{driverId}")
    Result<Boolean> sendOrderBillInfo(
            @PathVariable("orderId") Long orderId,
            @PathVariable("driverId") Long driverId
    ) {
        return Result.ok();
    }

    @Operation(summary = "获取订单支付信息")
    @GetMapping("/getOrderPayVo/{orderNo}/{customerId}")
    Result<OrderPayVo> getOrderPayVo(
            @PathVariable("orderNo") String orderNo,
            @PathVariable("customerId") Long customerId
    ) {
        return Result.ok();
    }

    @Operation(summary = "更改订单支付状态")
    @GetMapping("/updateOrderPayStatus/{orderNo}")
    Result<Boolean> updateOrderPayStatus(@PathVariable("orderNo") String orderNo) {
        return Result.ok();
    }

    @Operation(summary = "获取订单的系统奖励")
    @GetMapping("/getOrderRewardFee/{orderNo}")
    Result<OrderRewardVo> getOrderRewardFee(@PathVariable("orderNo") String orderNo) {
        return Result.ok();
    }

    @Operation(summary = "更新优惠券金额")
    @GetMapping("/updateCouponAmount/{orderId}/{couponAmount}")
    Result<Boolean> updateCouponAmount(
            @PathVariable("orderId") Long orderId,
            @PathVariable("couponAmount") BigDecimal couponAmount
    ) {
        return Result.ok();
    }
}

