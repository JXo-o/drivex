package com.jxh.drivex.order.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.order.OrderMonitor;
import com.jxh.drivex.model.entity.order.OrderMonitorRecord;
import com.jxh.drivex.order.service.OrderMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/monitor")
public class OrderMonitorController {

    private final OrderMonitorService orderMonitorService;

    public OrderMonitorController(OrderMonitorService orderMonitorService) {
        this.orderMonitorService = orderMonitorService;
    }

    @Operation(summary = "保存订单监控记录数据")
    @PostMapping("/saveOrderMonitorRecord")
    Result<Boolean> saveMonitorRecord(@RequestBody OrderMonitorRecord orderMonitorRecord) {
        return Result.ok(orderMonitorService.saveOrderMonitorRecord(orderMonitorRecord));
    }

    @Operation(summary = "根据订单id获取订单监控信息")
    @GetMapping("/getOrderMonitor/{orderId}")
    public Result<OrderMonitor> getOrderMonitor(@PathVariable Long orderId) {
        return Result.ok(orderMonitorService.getOrderMonitor(orderId));
    }

    @Operation(summary = "更新订单监控信息")
    @PostMapping("/updateOrderMonitor")
    public Result<Boolean> updateOrderMonitor(@RequestBody OrderMonitor OrderMonitor) {
        return Result.ok(orderMonitorService.updateOrderMonitor(OrderMonitor));
    }
}

