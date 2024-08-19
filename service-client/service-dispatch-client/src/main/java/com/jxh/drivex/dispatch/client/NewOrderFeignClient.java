package com.jxh.drivex.dispatch.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.vo.dispatch.NewOrderTaskVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-dispatch")
public interface NewOrderFeignClient {

    /**
     * 添加新订单任务
     */
    @PostMapping("/dispatch/newOrder/addAndStartTask")
    Result<Long> addAndStartTask(@RequestBody NewOrderTaskVo newOrderDispatchVo);

    /**
     * 查询司机新订单数据
     */
    @GetMapping("/dispatch/newOrder/findNewOrderQueueData/{driverId}")
    Result<List<NewOrderDataVo>> findNewOrderQueueData(@PathVariable("driverId") Long driverId);

    /**
     * 清空新订单队列数据
     */
    @GetMapping("/dispatch/newOrder/clearNewOrderQueueData/{driverId}")
    Result<Boolean> clearNewOrderQueueData(@PathVariable("driverId") Long driverId);
}