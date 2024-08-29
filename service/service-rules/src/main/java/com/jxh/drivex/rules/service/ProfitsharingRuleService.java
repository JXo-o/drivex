package com.jxh.drivex.rules.service;

import com.jxh.drivex.model.form.rules.ProfitsharingRuleRequestForm;
import com.jxh.drivex.model.vo.rules.ProfitsharingRuleResponseVo;

public interface ProfitsharingRuleService {

    ProfitsharingRuleResponseVo calculateOrderProfitsharingFee(
            ProfitsharingRuleRequestForm profitsharingRuleRequestForm
    );
}
