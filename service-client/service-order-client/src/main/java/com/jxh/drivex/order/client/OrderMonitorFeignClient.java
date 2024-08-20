package com.jxh.drivex.order.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.order.OrderMonitorRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "service-order", contextId = "orderMonitor")
public interface OrderMonitorFeignClient {

    /**
     * 保存订单监控记录数据
     */
    @PostMapping("/order/monitor/saveOrderMonitorRecord")
    Result<Boolean> saveMonitorRecord(@RequestBody OrderMonitorRecord orderMonitorRecord);
}