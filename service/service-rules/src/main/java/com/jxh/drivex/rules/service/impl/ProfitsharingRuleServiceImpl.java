package com.jxh.drivex.rules.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxh.drivex.model.entity.rule.ProfitsharingRule;
import com.jxh.drivex.model.form.rules.ProfitsharingRuleRequest;
import com.jxh.drivex.model.form.rules.ProfitsharingRuleRequestForm;
import com.jxh.drivex.model.vo.rules.ProfitsharingRuleResponse;
import com.jxh.drivex.model.vo.rules.ProfitsharingRuleResponseVo;
import com.jxh.drivex.model.vo.rules.RewardRuleResponseVo;
import com.jxh.drivex.rules.mapper.ProfitsharingRuleMapper;
import com.jxh.drivex.rules.mapper.RewardRuleMapper;
import com.jxh.drivex.rules.service.ProfitsharingRuleService;
import com.jxh.drivex.rules.utils.DroolsUtil;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProfitsharingRuleServiceImpl implements ProfitsharingRuleService {

    private final ProfitsharingRuleMapper profitsharingRuleMapper;

    public ProfitsharingRuleServiceImpl(ProfitsharingRuleMapper profitsharingRuleMapper) {
        this.profitsharingRuleMapper = profitsharingRuleMapper;
    }

    /**
     * 计算订单分账数据.
     * <p>
     * 该方法根据传入的分账规则请求表单对象来计算订单分账数据。首先，从数据库中获取最新的分账规则，
     * 然后使用 Drools 引擎根据这些规则对分账数据进行计算。计算结果将封装到 {@link ProfitsharingRuleResponseVo} 对象中并返回。
     * </p>
     *
     * @param profitsharingRuleRequestForm 分账规则请求表单对象，包含计算订单分账数据所需的信息，例如订单号。
     * @return 返回一个 {@link ProfitsharingRuleResponseVo} 对象，其中包含计算后的订单分账数据和相关信息。
     */
    @Override
    public ProfitsharingRuleResponseVo calculateOrderProfitsharingFee(
            ProfitsharingRuleRequestForm profitsharingRuleRequestForm
    ) {
        ProfitsharingRuleRequest profitsharingRuleRequest = new ProfitsharingRuleRequest();
        profitsharingRuleRequest.setOrderAmount(profitsharingRuleRequestForm.getOrderAmount());
        profitsharingRuleRequest.setOrderNum(profitsharingRuleRequestForm.getOrderNum());
        log.info("传入参数：{}", JSON.toJSONString(profitsharingRuleRequest));

        ProfitsharingRule profitsharingRule = profitsharingRuleMapper.selectOne(
                new LambdaQueryWrapper<ProfitsharingRule>()
                        .orderByDesc(ProfitsharingRule::getId)
                        .last("limit 1")
        );
        KieSession kieSession = DroolsUtil.loadForRule(profitsharingRule.getRule());
        ProfitsharingRuleResponse profitsharingRuleResponse = new ProfitsharingRuleResponse();
        kieSession.setGlobal("profitsharingRuleResponse", profitsharingRuleResponse);
        kieSession.insert(profitsharingRuleRequest);
        kieSession.fireAllRules();
        kieSession.dispose();
        log.info("计算结果：{}", JSON.toJSONString(profitsharingRuleResponse));

        ProfitsharingRuleResponseVo profitsharingRuleResponseVo = new ProfitsharingRuleResponseVo();
        profitsharingRuleResponseVo.setProfitsharingRuleId(profitsharingRule.getId());
        BeanUtils.copyProperties(profitsharingRuleResponse, profitsharingRuleResponseVo);
        return profitsharingRuleResponseVo;
    }
}
