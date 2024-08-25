package com.jxh.drivex.customer.service.impl;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.login.jwt.JwtUtil;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.customer.client.CustomerInfoFeignClient;
import com.jxh.drivex.customer.service.CustomerService;
import com.jxh.drivex.model.form.customer.UpdateWxPhoneForm;
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

    /**
     * 登录方法
     * <p>
     * 该方法通过调用 `customerInfoFeignClient` 的 `login` 方法，传入微信授权码（code），获取用户 ID。
     * 然后，使用 `jwtUtil` 创建并返回一个包含用户 ID 的 JWT 令牌。
     * </p>
     *
     * @param code 微信授权码
     * @return 生成的 JWT 令牌
     */
    @Override
    public String login(String code) {
        Result<Long> loginResult = customerInfoFeignClient.login(code);
        return jwtUtil.createToken(loginResult.getData());
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        return customerInfoFeignClient.getCustomerLoginInfo(customerId).getData();
    }

    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        return customerInfoFeignClient.updateWxPhoneNumber(updateWxPhoneForm).getData();
    }
}
