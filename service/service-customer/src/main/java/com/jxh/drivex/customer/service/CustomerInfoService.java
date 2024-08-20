package com.jxh.drivex.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxh.drivex.model.entity.customer.CustomerInfo;
import com.jxh.drivex.model.vo.customer.CustomerLoginVo;

public interface CustomerInfoService extends IService<CustomerInfo> {

    Long login(String code);

    CustomerLoginVo getCustomerLoginInfo(Long customerId);
}
