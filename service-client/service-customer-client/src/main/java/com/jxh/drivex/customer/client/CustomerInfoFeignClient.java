package com.jxh.drivex.customer.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.customer.UpdateWxPhoneForm;
import com.jxh.drivex.model.vo.customer.CustomerLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-customer")
public interface CustomerInfoFeignClient {

    @GetMapping("/customer/info/login/{code}")
    public Result<Long> login(@PathVariable String code);

    @GetMapping("/customer/info/getCustomerLoginInfo/{customerId}")
    Result<CustomerLoginVo> getCustomerLoginInfo(@PathVariable("customerId") Long customerId);

    /**
     * 更新客户微信手机号码
     */
    @PostMapping("/customer/info/updateWxPhoneNumber")
    Result<Boolean> updateWxPhoneNumber(@RequestBody UpdateWxPhoneForm updateWxPhoneForm);

    /**
     * 获取客户OpenId
     */
    @GetMapping("/customer/info/getCustomerOpenId/{customerId}")
    Result<String> getCustomerOpenId(@PathVariable("customerId") Long customerId);
}