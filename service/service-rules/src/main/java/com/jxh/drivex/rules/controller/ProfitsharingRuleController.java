package com.jxh.drivex.rules.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.rules.ProfitsharingRuleRequestForm;
import com.jxh.drivex.model.vo.rules.ProfitsharingRuleResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rules/profitsharing")
public class ProfitsharingRuleController {

    @Operation(summary = "计算订单分账数据")
    @PostMapping("/calculateOrderProfitsharingFee")
    Result<ProfitsharingRuleResponseVo> calculateOrderProfitsharingFee(
            @RequestBody ProfitsharingRuleRequestForm profitsharingRuleRequestForm) {
        return Result.ok();
    }

}

