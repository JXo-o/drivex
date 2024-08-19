package com.jxh.drivex.driver.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.driver.TransferForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-driver")
public interface DriverAccountFeignClient {

    /**
     * 转账
     */
    @PostMapping("/driver/account/transfer")
    Result<Boolean> transfer(@RequestBody TransferForm transferForm);
}