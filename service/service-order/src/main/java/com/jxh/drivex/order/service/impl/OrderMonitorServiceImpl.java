package com.jxh.drivex.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.model.entity.order.OrderMonitor;
import com.jxh.drivex.order.mapper.OrderMonitorMapper;
import com.jxh.drivex.order.service.OrderMonitorService;
import org.springframework.stereotype.Service;

@Service
public class OrderMonitorServiceImpl extends ServiceImpl<OrderMonitorMapper, OrderMonitor>
        implements OrderMonitorService {

}
