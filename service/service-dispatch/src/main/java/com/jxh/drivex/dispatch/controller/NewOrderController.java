package com.jxh.drivex.dispatch.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.dispatch.service.NewOrderService;
import com.jxh.drivex.model.vo.dispatch.NewOrderTaskVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "司机新订单接口管理")
@RestController
@RequestMapping("/dispatch/newOrder")
public class NewOrderController {

    private final NewOrderService newOrderService;

    public NewOrderController(NewOrderService newOrderService) {
        this.newOrderService = newOrderService;
    }

    @Operation(summary = "添加并开始新订单任务调度")
    @PostMapping("/addAndStartTask")
    Result<Long> addAndStartTask(@RequestBody NewOrderTaskVo newOrderTaskVo) {
        return Result.ok(newOrderService.addAndStartTask(newOrderTaskVo));
    }

    @Operation(summary = "查询司机新订单数据")
    @GetMapping("/findNewOrderQueueData/{driverId}")
    Result<List<NewOrderDataVo>> findNewOrderQueueData(@PathVariable("driverId") Long driverId) {
        return Result.ok(newOrderService.findNewOrderQueueData(driverId));
    }

    @Operation(summary = "清空新订单队列数据")
    @GetMapping("/clearNewOrderQueueData/{driverId}")
    Result<Boolean> clearNewOrderQueueData(@PathVariable("driverId") Long driverId) {
        return Result.ok(newOrderService.clearNewOrderQueueData(driverId));
    }
}

