package com.jxh.drivex.rules.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.rules.RewardRuleRequestForm;
import com.jxh.drivex.model.vo.rules.RewardRuleResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-rules")
public interface RewardRuleFeignClient {

    /**
     * 计算订单奖励费用
     */
    @PostMapping("/rules/reward/calculateOrderRewardFee")
    Result<RewardRuleResponseVo> calculateOrderRewardFee(@RequestBody RewardRuleRequestForm rewardRuleRequestForm);
}