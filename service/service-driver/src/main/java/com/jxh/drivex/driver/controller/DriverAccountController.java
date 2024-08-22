package com.jxh.drivex.driver.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.driver.TransferForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "司机账户API接口管理")
@RestController
@RequestMapping(value="/driver/account")
public class DriverAccountController {

    @Operation(summary = "转账")
    @PostMapping("/transfer")
    Result<Boolean> transfer(@RequestBody TransferForm transferForm) {
        return Result.ok(true);
    }

}

