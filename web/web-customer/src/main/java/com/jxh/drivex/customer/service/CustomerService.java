package com.jxh.drivex.customer.service;

import com.jxh.drivex.model.vo.customer.CustomerLoginVo;

public interface CustomerService {

    String login(String code);

    CustomerLoginVo getCustomerLoginInfo(Long customerId);
}
