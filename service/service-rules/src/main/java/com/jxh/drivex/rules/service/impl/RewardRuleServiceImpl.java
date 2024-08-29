package com.jxh.drivex.rules.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxh.drivex.model.entity.rule.RewardRule;
import com.jxh.drivex.model.form.rules.RewardRuleRequest;
import com.jxh.drivex.model.form.rules.RewardRuleRequestForm;
import com.jxh.drivex.model.vo.rules.FeeRuleResponseVo;
import com.jxh.drivex.model.vo.rules.RewardRuleResponse;
import com.jxh.drivex.model.vo.rules.RewardRuleResponseVo;
import com.jxh.drivex.rules.mapper.RewardRuleMapper;
import com.jxh.drivex.rules.service.RewardRuleService;
import com.jxh.drivex.rules.utils.DroolsUtil;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RewardRuleServiceImpl implements RewardRuleService {

    private final RewardRuleMapper rewardRuleMapper;

    public RewardRuleServiceImpl(RewardRuleMapper rewardRuleMapper) {
        this.rewardRuleMapper = rewardRuleMapper;
    }

    /**
     * 计算订单奖励费用。
     * <p>
     * 该方法根据传入的奖励费用规则请求表单对象来计算订单奖励费用。首先，从数据库中获取最新的奖励费用规则，
     * 然后使用 Drools 引擎根据这些规则对奖励费用进行计算。计算结果将封装到 {@link RewardRuleResponseVo} 对象中并返回。
     * </p>
     *
     * @param rewardRuleRequestForm 奖励费用规则请求表单对象，包含计算订单奖励费用所需的信息，例如订单号。
     * @return 返回一个 {@link RewardRuleResponseVo} 对象，其中包含计算后的订单奖励费用和相关信息。
     */
    @Override
    public RewardRuleResponseVo calculateOrderRewardFee(RewardRuleRequestForm rewardRuleRequestForm) {
        RewardRuleRequest rewardRuleRequest = new RewardRuleRequest();
        rewardRuleRequest.setOrderNum(rewardRuleRequestForm.getOrderNum());
        log.info("传入参数：{}", JSON.toJSONString(rewardRuleRequest));

        RewardRule rewardRule = rewardRuleMapper.selectOne(
                new LambdaQueryWrapper<RewardRule>()
                        .orderByDesc(RewardRule::getId)
                        .last("limit 1")
        );
        KieSession kieSession = DroolsUtil.loadForRule(rewardRule.getRule());
        RewardRuleResponse rewardRuleResponse = new RewardRuleResponse();
        kieSession.setGlobal("rewardRuleResponse", rewardRuleResponse);
        kieSession.insert(rewardRuleRequest);
        kieSession.fireAllRules();
        kieSession.dispose();
        log.info("计算结果：{}", JSON.toJSONString(rewardRuleResponse));

        RewardRuleResponseVo rewardRuleResponseVo = new RewardRuleResponseVo();
        rewardRuleResponseVo.setRewardRuleId(rewardRule.getId());
        rewardRuleResponseVo.setRewardAmount(rewardRuleResponse.getRewardAmount());
        return rewardRuleResponseVo;
    }
}
