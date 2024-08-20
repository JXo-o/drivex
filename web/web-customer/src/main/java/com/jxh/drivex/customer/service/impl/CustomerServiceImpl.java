package com.jxh.drivex.customer.service.impl;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.login.jwt.JwtUtil;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.customer.client.CustomerInfoFeignClient;
import com.jxh.drivex.customer.service.CustomerService;
import com.jxh.drivex.model.vo.customer.CustomerLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final JwtUtil jwtUtil;
    private final CustomerInfoFeignClient customerInfoFeignClient;

    public CustomerServiceImpl(
            JwtUtil jwtUtil,
            CustomerInfoFeignClient customerInfoFeignClient
    ) {
        this.jwtUtil = jwtUtil;
        this.customerInfoFeignClient = customerInfoFeignClient;
    }

    @Override
    public String login(String code) {
        Result<Long> loginResult = customerInfoFeignClient.login(code);
        if (!loginResult.isOk()) {
            throw new DrivexException(loginResult.getCode(), loginResult.getMessage());
        }
        return jwtUtil.createToken(loginResult.getData());
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        Result<CustomerLoginVo> result = customerInfoFeignClient.getCustomerLoginInfo(customerId);
        if(!result.isOk()) {
            throw new DrivexException(result.getCode(), result.getMessage());
        }
        return result.getData();
    }
}
