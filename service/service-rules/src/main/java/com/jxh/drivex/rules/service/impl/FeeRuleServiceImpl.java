package com.jxh.drivex.rules.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxh.drivex.model.entity.rule.FeeRule;
import com.jxh.drivex.model.form.rules.FeeRuleRequest;
import com.jxh.drivex.model.form.rules.FeeRuleRequestForm;
import com.jxh.drivex.model.vo.rules.FeeRuleResponse;
import com.jxh.drivex.model.vo.rules.FeeRuleResponseVo;
import com.jxh.drivex.rules.mapper.FeeRuleMapper;
import com.jxh.drivex.rules.service.FeeRuleService;
import com.jxh.drivex.rules.utils.DroolsUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FeeRuleServiceImpl implements FeeRuleService {

    private final FeeRuleMapper feeRuleMapper;

    public FeeRuleServiceImpl(FeeRuleMapper feeRuleMapper) {
        this.feeRuleMapper = feeRuleMapper;
    }

    /**
     * 计算订单费用。
     * <p>
     * 该方法根据传入的费用规则请求表单对象来计算订单费用。首先，从数据库中获取最新的费用规则，
     * 然后使用 Drools 引擎根据这些规则对费用进行计算。计算结果将封装到 {@link FeeRuleResponseVo} 对象中并返回。
     * </p>
     *
     * @param feeRuleRequestForm 费用规则请求表单对象，包含计算订单费用所需的信息，例如距离、开始时间和等待时间。
     * @return 返回一个 {@link FeeRuleResponseVo} 对象，其中包含计算后的订单费用和相关信息。
     */
    @Override
    public FeeRuleResponseVo calculateOrderFee(FeeRuleRequestForm feeRuleRequestForm) {
        FeeRuleRequest feeRuleRequest = new FeeRuleRequest();
        feeRuleRequest.setDistance(feeRuleRequestForm.getDistance());
        feeRuleRequest.setStartTime(new DateTime(feeRuleRequestForm.getStartTime()).toString("HH:mm:ss"));
        feeRuleRequest.setWaitMinute(feeRuleRequestForm.getWaitMinute());
        log.info("传入参数：{}", JSON.toJSONString(feeRuleRequest));

        FeeRule feeRule = feeRuleMapper.selectOne(
                new LambdaQueryWrapper<FeeRule>()
                        .orderByDesc(FeeRule::getId)
                        .last("limit 1")
        );
        KieSession kieSession = DroolsUtil.loadForRule(feeRule.getRule());
        FeeRuleResponse feeRuleResponse = new FeeRuleResponse();
        kieSession.setGlobal("feeRuleResponse", feeRuleResponse);
        kieSession.insert(feeRuleRequest);
        kieSession.fireAllRules();
        kieSession.dispose();
        log.info("计算结果：{}", JSON.toJSONString(feeRuleResponse));

        FeeRuleResponseVo feeRuleResponseVo = new FeeRuleResponseVo();
        feeRuleResponseVo.setFeeRuleId(feeRule.getId());
        BeanUtils.copyProperties(feeRuleResponse, feeRuleResponseVo);
        return feeRuleResponseVo;
    }
}
