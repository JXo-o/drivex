package com.jxh.drivex.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxh.drivex.model.entity.order.OrderMonitor;
import com.jxh.drivex.model.entity.order.OrderMonitorRecord;

public interface OrderMonitorService extends IService<OrderMonitor> {

    Boolean saveOrderMonitorRecord(OrderMonitorRecord orderMonitorRecord);

    OrderMonitor getOrderMonitor(Long orderId);

    Boolean updateOrderMonitor(OrderMonitor orderMonitor);

    void saveOrderMonitor(OrderMonitor orderMonitor);
}
