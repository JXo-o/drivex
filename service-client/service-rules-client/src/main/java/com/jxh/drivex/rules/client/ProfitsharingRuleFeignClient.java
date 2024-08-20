package com.jxh.drivex.rules.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.rules.ProfitsharingRuleRequestForm;
import com.jxh.drivex.model.vo.rules.ProfitsharingRuleResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-rules", contextId = "profitsharingRule")
public interface ProfitsharingRuleFeignClient {

    /**
     * 计算订单分账数据
     */
    @PostMapping("/rules/profitsharing/calculateOrderProfitsharingFee")
    Result<ProfitsharingRuleResponseVo> calculateOrderProfitsharingFee(
            @RequestBody ProfitsharingRuleRequestForm profitsharingRuleRequestForm);
}