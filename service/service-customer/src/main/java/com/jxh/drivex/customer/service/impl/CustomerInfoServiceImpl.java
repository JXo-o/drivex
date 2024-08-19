package com.jxh.drivex.customer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.customer.mapper.CustomerInfoMapper;
import com.jxh.drivex.customer.service.CustomerInfoService;
import com.jxh.drivex.model.entity.customer.CustomerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo>
        implements CustomerInfoService {

}
