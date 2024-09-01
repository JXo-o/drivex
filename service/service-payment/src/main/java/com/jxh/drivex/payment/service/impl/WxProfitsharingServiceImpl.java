package com.jxh.drivex.payment.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxh.drivex.common.constant.MqConst;
import com.jxh.drivex.common.constant.SystemConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.common.service.RabbitService;
import com.jxh.drivex.model.entity.payment.PaymentInfo;
import com.jxh.drivex.model.entity.payment.ProfitsharingInfo;
import com.jxh.drivex.model.form.payment.ProfitsharingForm;
import com.jxh.drivex.payment.config.WxPayProperties;
import com.jxh.drivex.payment.mapper.PaymentInfoMapper;
import com.jxh.drivex.payment.mapper.ProfitsharingInfoMapper;
import com.jxh.drivex.payment.service.WxProfitsharingService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.profitsharing.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class WxProfitsharingServiceImpl implements WxProfitsharingService {

    private final PaymentInfoMapper paymentInfoMapper;

    private final ProfitsharingInfoMapper profitsharingInfoMapper;

    private final WxPayProperties wxPayProperties;

    private final RSAAutoCertificateConfig rsaAutoCertificateConfig;

    private final RabbitService rabbitService;

    public WxProfitsharingServiceImpl(
            PaymentInfoMapper paymentInfoMapper,
            ProfitsharingInfoMapper profitsharingInfoMapper,
            WxPayProperties wxPayProperties,
            RSAAutoCertificateConfig rsaAutoCertificateConfig,
            RabbitService rabbitService
    ) {
        this.paymentInfoMapper = paymentInfoMapper;
        this.profitsharingInfoMapper = profitsharingInfoMapper;
        this.wxPayProperties = wxPayProperties;
        this.rsaAutoCertificateConfig = rsaAutoCertificateConfig;
        this.rabbitService = rabbitService;
    }

    /**
     * 执行微信支付分账操作。
     *
     * <p>该方法的主要操作流程如下：</p>
     * <ul>
     *     <li>首先检查数据库中是否已存在该订单号的分账记录，如果存在则直接返回，避免重复分账。</li>
     *     <li>根据订单号从数据库中获取对应的支付信息 {@link PaymentInfo}。</li>
     *     <li>使用支付信息中的商户号和司机的微信OpenID，向微信支付平台添加一个分账接收方。</li>
     *     <li>构建微信分账订单请求，包含分账接收方信息和分账金额，调用微信支付平台进行分账操作。</li>
     *     <li>根据分账操作的结果：
     *          <ul>
     *              <li>如果分账成功（状态为 FINISHED），将分账信息保存到数据库，并通过消息队列通知相关系统分账成功。</li>
     *              <li>如果分账处理中（状态为 PROCESSING），将该分账请求延迟发送到消息队列，以便稍后重试分账操作。</li>
     *              <li>如果分账失败，记录错误日志并抛出自定义异常 {@link DrivexException}。</li>
     *          </ul>
     *     </li>
     * </ul>
     *
     * @param profitsharingForm 分账请求表单对象，包含分账所需的订单号和分账金额等信息。
     * @throws DrivexException 当微信分账操作失败时抛出该异常。
     */
    @Override
    public void profitsharing(ProfitsharingForm profitsharingForm) {
        long count = profitsharingInfoMapper.selectCount(
                new LambdaQueryWrapper<ProfitsharingInfo>()
                        .eq(ProfitsharingInfo::getOrderNo, profitsharingForm.getOrderNo())
        );
        if(count > 0) return;

        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(
                new LambdaQueryWrapper<PaymentInfo>()
                        .eq(PaymentInfo::getOrderNo, profitsharingForm.getOrderNo())
        );
        ProfitsharingService profitsharingService = new ProfitsharingService
                .Builder()
                .config(rsaAutoCertificateConfig)
                .build();

        AddReceiverRequest addReceiverRequest = new AddReceiverRequest();
        addReceiverRequest.setAppid(wxPayProperties.getAppid());
        addReceiverRequest.setType(ReceiverType.PERSONAL_OPENID);
        addReceiverRequest.setAccount(paymentInfo.getDriverOpenId());
        addReceiverRequest.setRelationType(ReceiverRelationType.PARTNER);
        AddReceiverResponse addReceiverResponse = profitsharingService.addReceiver(addReceiverRequest);
        log.info("添加分账接收方：{}", JSON.toJSONString(addReceiverResponse));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAppid(wxPayProperties.getAppid());
        request.setTransactionId(paymentInfo.getTransactionId());
        String outOrderNo = profitsharingForm.getOrderNo() + "_" + new Random().nextInt(10);
        request.setOutOrderNo(outOrderNo);

        List<CreateOrderReceiver> receivers = new ArrayList<>();
        CreateOrderReceiver orderReceiver = new CreateOrderReceiver();
        orderReceiver.setType("PERSONAL_OPENID");
        orderReceiver.setAccount(paymentInfo.getDriverOpenId());
        Long amount = profitsharingForm.getAmount().multiply(new BigDecimal("100")).longValue();
        orderReceiver.setAmount(amount);
        orderReceiver.setDescription("司机代驾分账");
        receivers.add(orderReceiver);

        request.setReceivers(receivers);
        request.setUnfreezeUnsplit(true);
        OrdersEntity ordersEntity = profitsharingService.createOrder(request);
        if(ordersEntity.getState().name().equals("FINISHED")) {
            ProfitsharingInfo profitsharingInfo = new ProfitsharingInfo();
            profitsharingInfo.setOrderNo(paymentInfo.getOrderNo());
            profitsharingInfo.setTransactionId(paymentInfo.getTransactionId());
            profitsharingInfo.setOutTradeNo(outOrderNo);
            profitsharingInfo.setAmount(profitsharingInfo.getAmount());
            profitsharingInfo.setState(ordersEntity.getState().name());
            profitsharingInfo.setResponeContent(JSON.toJSONString(ordersEntity));
            profitsharingInfoMapper.insert(profitsharingInfo);
            rabbitService.sendMessage(
                    MqConst.EXCHANGE_ORDER,
                    MqConst.ROUTING_PROFITSHARING_SUCCESS,
                    paymentInfo.getOrderNo()
            );
        } else if(ordersEntity.getState().name().equals("PROCESSING")) {
            rabbitService.sendDelayMessage(
                    MqConst.EXCHANGE_PROFITSHARING,
                    MqConst.ROUTING_PROFITSHARING,
                    JSON.toJSONString(profitsharingForm),
                    SystemConstant.PROFITSHARING_DELAY_TIME
            );
        } else {
            log.error("执行分账失败");
            throw new DrivexException(ResultCodeEnum.WX_PROFITSHARING_FAILURE);
        }
    }
}
