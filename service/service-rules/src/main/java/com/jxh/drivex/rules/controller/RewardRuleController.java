package com.jxh.drivex.rules.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.rules.RewardRuleRequestForm;
import com.jxh.drivex.model.vo.rules.RewardRuleResponseVo;
import com.jxh.drivex.rules.service.RewardRuleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rules/reward")
public class RewardRuleController {

    private final RewardRuleService rewardRuleService;

    public RewardRuleController(RewardRuleService rewardRuleService) {
        this.rewardRuleService = rewardRuleService;
    }

    @Operation(summary = "计算订单奖励费用")
    @PostMapping("/calculateOrderRewardFee")
    Result<RewardRuleResponseVo> calculateOrderRewardFee(@RequestBody RewardRuleRequestForm rewardRuleRequestForm) {
        return Result.ok(rewardRuleService.calculateOrderRewardFee(rewardRuleRequestForm));
    }

}

