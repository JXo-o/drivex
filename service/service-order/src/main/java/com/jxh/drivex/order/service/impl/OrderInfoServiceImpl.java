package com.jxh.drivex.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.order.mapper.OrderInfoMapper;
import com.jxh.drivex.order.service.OrderInfoService;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

}
