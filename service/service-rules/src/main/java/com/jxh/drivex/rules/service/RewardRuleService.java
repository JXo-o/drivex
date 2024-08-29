package com.jxh.drivex.rules.service;

import com.jxh.drivex.model.form.rules.RewardRuleRequestForm;
import com.jxh.drivex.model.vo.rules.RewardRuleResponseVo;

public interface RewardRuleService {

    RewardRuleResponseVo calculateOrderRewardFee(RewardRuleRequestForm rewardRuleRequestForm);
}
