package com.jxh.drivex.rules.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.rules.FeeRuleRequestForm;
import com.jxh.drivex.model.vo.rules.FeeRuleResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-rules", contextId = "feeRule")
public interface FeeRuleFeignClient {

    /**
     * 计算订单费用
     */
    @PostMapping("/rules/fee/calculateOrderFee")
    Result<FeeRuleResponseVo> calculateOrderFee(@RequestBody FeeRuleRequestForm calculateOrderFeeForm);
}