package com.jxh.drivex.customer.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.customer.service.CustomerInfoService;
import com.jxh.drivex.model.form.customer.UpdateWxPhoneForm;
import com.jxh.drivex.model.vo.customer.CustomerLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/customer/info")
public class CustomerInfoController {

    private final CustomerInfoService customerInfoService;

    public CustomerInfoController(CustomerInfoService customerInfoService) {
        this.customerInfoService = customerInfoService;
    }

    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<Long> login(@PathVariable("code") String code) {
        log.info("login code:{}", code);
        return Result.ok(customerInfoService.login(code));
    }

    @Operation(summary = "获取客户登录信息")
    @GetMapping("/getCustomerLoginInfo/{customerId}")
    Result<CustomerLoginVo> getCustomerLoginInfo(@PathVariable("customerId") Long customerId) {
        log.info("getCustomerLoginInfo customerId:{}", customerId);
        return Result.ok(customerInfoService.getCustomerLoginInfo(customerId));
    }

    @Operation(summary = "更新客户微信手机号码")
    @PostMapping("/updateWxPhoneNumber")
    Result<Boolean> updateWxPhoneNumber(@RequestBody UpdateWxPhoneForm updateWxPhoneForm) {
        log.info("updateWxPhoneNumber updateWxPhoneForm:{}", updateWxPhoneForm);
        return Result.ok(customerInfoService.updateWxPhoneNumber(updateWxPhoneForm));
    }

    @Operation(summary = "获取客户OpenId")
    @GetMapping("/getCustomerOpenId/{customerId}")
    Result<String> getCustomerOpenId(@PathVariable("customerId") Long customerId) {
        log.info("getCustomerOpenId customerId:{}", customerId);
        return Result.ok("openId");
    }

}

