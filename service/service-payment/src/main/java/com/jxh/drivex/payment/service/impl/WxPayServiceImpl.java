package com.jxh.drivex.payment.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxh.drivex.common.constant.MqConst;
import com.jxh.drivex.common.constant.SystemConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.service.RabbitService;
import com.jxh.drivex.common.util.RequestUtils;
import com.jxh.drivex.driver.client.DriverAccountFeignClient;
import com.jxh.drivex.model.entity.payment.PaymentInfo;
import com.jxh.drivex.model.enums.TradeType;
import com.jxh.drivex.model.form.driver.TransferForm;
import com.jxh.drivex.model.form.payment.PaymentInfoForm;
import com.jxh.drivex.model.form.payment.ProfitsharingForm;
import com.jxh.drivex.model.vo.order.OrderProfitsharingVo;
import com.jxh.drivex.model.vo.order.OrderRewardVo;
import com.jxh.drivex.model.vo.payment.WxPrepayVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import com.jxh.drivex.payment.config.WxPayProperties;
import com.jxh.drivex.payment.mapper.PaymentInfoMapper;
import com.jxh.drivex.payment.service.WxPayService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class WxPayServiceImpl implements WxPayService {

    private final PaymentInfoMapper paymentInfoMapper;
    private final RSAAutoCertificateConfig rsaAutoCertificateConfig;
    private final WxPayProperties wxPayProperties;
    private final RabbitService rabbitService;
    private final OrderInfoFeignClient orderInfoFeignClient;
    private final DriverAccountFeignClient driverAccountFeignClient;

    public WxPayServiceImpl(
            PaymentInfoMapper paymentInfoMapper,
            RSAAutoCertificateConfig rsaAutoCertificateConfig,
            WxPayProperties wxPayProperties,
            RabbitService rabbitService,
            OrderInfoFeignClient orderInfoFeignClient,
            DriverAccountFeignClient driverAccountFeignClient
    ) {
        this.paymentInfoMapper = paymentInfoMapper;
        this.rsaAutoCertificateConfig = rsaAutoCertificateConfig;
        this.wxPayProperties = wxPayProperties;
        this.rabbitService = rabbitService;
        this.orderInfoFeignClient = orderInfoFeignClient;
        this.driverAccountFeignClient = driverAccountFeignClient;
    }

    /**
     * 创建微信支付
     * <p>
     * 该方法用于创建微信支付订单，并返回预支付信息。
     * 它首先检查订单是否已存在，如果不存在则创建一个新的订单。
     * 然后通过微信支付API创建预支付订单，并返回相关的支付信息。
     * </p>
     *
     * @param paymentInfoForm 包含支付信息的表单对象
     * @return WxPrepayVo 包含预支付信息的对象
     * @throws DrivexException 如果微信支付下单失败，则抛出该异常
     */
    @Override
    public WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm) {
        PaymentInfo paymentInfo = Optional.ofNullable(
                paymentInfoMapper.selectOne(
                        new LambdaQueryWrapper<PaymentInfo>()
                                .eq(PaymentInfo::getOrderNo, paymentInfoForm.getOrderNo())
                )
        ).orElseGet(() -> {
            PaymentInfo newPaymentInfo = new PaymentInfo();
            BeanUtils.copyProperties(paymentInfoForm, newPaymentInfo);
            newPaymentInfo.setPaymentStatus(0);
            paymentInfoMapper.insert(newPaymentInfo);
            return newPaymentInfo;
        });

        JsapiServiceExtension jsapiServiceExtension = new JsapiServiceExtension
                .Builder().config(rsaAutoCertificateConfig).build();
        PrepayRequest request = getPrepayRequest(paymentInfoForm, paymentInfo);
        PrepayWithRequestPaymentResponse prepayWithRequestPaymentResponse =
                jsapiServiceExtension.prepayWithRequestPayment(request);
        log.info("微信支付下单返回参数：{}", JSON.toJSONString(prepayWithRequestPaymentResponse));

        WxPrepayVo wxPrepayVo = new WxPrepayVo();
        BeanUtils.copyProperties(prepayWithRequestPaymentResponse, wxPrepayVo);
        wxPrepayVo.setTimeStamp(prepayWithRequestPaymentResponse.getTimeStamp());
        return wxPrepayVo;
    }

    /**
     * 微信支付回调
     *
     * @param request 请求对象
     * @return 回调结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> wxNotify(HttpServletRequest request) {
        Map<String,Object> result = new HashMap<>();
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .body(RequestUtils.readData(request))
                    .build();
            NotificationParser parser = new NotificationParser(rsaAutoCertificateConfig);
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("成功解析：{}", JSON.toJSONString(transaction));
            if(null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                this.handlePayment(transaction);
            }
            result.put("code", "SUCCESS");
            result.put("message", "成功");
        } catch (Exception e) {
            log.error("微信支付回调失败", e);
            result.put("code", "FAIL");
            result.put("message", "失败");
        }
        return result;
    }

    /**
     * 查询支付状态
     *
     * @param orderNo 订单号
     * @return 支付状态
     */
    @Override
    public Boolean queryPayStatus(String orderNo) {
        JsapiServiceExtension jsapiServiceExtension = new JsapiServiceExtension
                .Builder()
                .config(rsaAutoCertificateConfig)
                .build();

        QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
        queryRequest.setMchid(wxPayProperties.getMerchantId());
        queryRequest.setOutTradeNo(orderNo);
        try {
            Transaction transaction = jsapiServiceExtension.queryOrderByOutTradeNo(queryRequest);
            log.info(JSON.toJSONString(transaction));
            if(null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                this.handlePayment(transaction);
                return true;
            }
        } catch (ServiceException e) {
            log.error("查询支付状态失败", e);
        }
        return false;
    }

    /**
     * 处理订单支付后的后续操作，包括更新订单支付状态、处理奖励费用以及延迟发送分账请求。
     *
     * <p>该方法的主要操作流程如下：</p>
     * <ul>
     *     <li>调用订单服务，更新订单的支付状态。</li>
     *     <li>获取订单的奖励费用信息 {@link OrderRewardVo}：
     *          <ul>
     *              <li>如果奖励费用大于0，则创建转账请求 {@link TransferForm} 并调用司机账户服务执行转账操作，将奖励费用支付给司机。</li>
     *          </ul>
     *     </li>
     *     <li>获取订单的分账信息 {@link OrderProfitsharingVo}：
     *          <ul>
     *              <li>构建分账请求 {@link ProfitsharingForm}，并将其以延迟消息的方式发送到消息队列，以便稍后进行分账操作。</li>
     *          </ul>
     *     </li>
     * </ul>
     * <p>此方法使用了 {@code @GlobalTransactional} 注解，表示整个操作过程是一个分布式事务。</p>
     *
     * @param orderNo 订单号，用于标识需要处理的订单。
     */
    @Override
    @GlobalTransactional
    public void handleOrder(String orderNo) {
        orderInfoFeignClient.updateOrderPayStatus(orderNo);
        OrderRewardVo orderRewardVo = orderInfoFeignClient.getOrderRewardFee(orderNo).getData();
        if(orderRewardVo.getRewardFee() != null && orderRewardVo.getRewardFee().doubleValue() > 0) {
            TransferForm transferForm = new TransferForm();
            transferForm.setTradeNo(orderNo);
            transferForm.setTradeType(TradeType.REWARD.getType());
            transferForm.setContent(TradeType.REWARD.getContent());
            transferForm.setAmount(orderRewardVo.getRewardFee());
            transferForm.setDriverId(orderRewardVo.getDriverId());
            driverAccountFeignClient.transfer(transferForm);
        }

        OrderProfitsharingVo orderProfitsharingVo =
                orderInfoFeignClient.getOrderProfitsharing(orderRewardVo.getOrderId()).getData();
        ProfitsharingForm profitsharingForm = new ProfitsharingForm();
        profitsharingForm.setOrderNo(orderNo);
        profitsharingForm.setAmount(orderProfitsharingVo.getDriverIncome());
        profitsharingForm.setDriverId(orderRewardVo.getDriverId());
        rabbitService.sendDelayMessage(
                MqConst.EXCHANGE_PROFITSHARING,
                MqConst.ROUTING_PROFITSHARING,
                JSON.toJSONString(profitsharingForm),
                SystemConstant.PROFITSHARING_DELAY_TIME
        );
    }

    /**
     * 获取预支付请求
     * <p>
     * 该方法用于创建微信支付的预支付请求对象。它将支付信息表单中的数据
     * 和订单信息转换为符合微信支付API的预支付请求格式。
     * </p>
     *
     * @param paymentInfoForm 包含支付信息的表单对象
     * @param paymentInfo 包含订单信息的对象
     * @return PrepayRequest 预支付请求对象，包含了必要的支付信息
     */
    private PrepayRequest getPrepayRequest(PaymentInfoForm paymentInfoForm, PaymentInfo paymentInfo) {
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(paymentInfoForm.getAmount().multiply(new BigDecimal(100)).intValue());
        request.setAmount(amount);
        request.setAppid(wxPayProperties.getAppid());
        request.setMchid(wxPayProperties.getMerchantId());

        String description = paymentInfo.getContent();
        request.setDescription(description.length() > 127 ? description.substring(0, 127) : description);
        request.setNotifyUrl(wxPayProperties.getNotifyUrl());
        request.setOutTradeNo(paymentInfo.getOrderNo());

        Payer payer = new Payer();
        payer.setOpenid(paymentInfoForm.getCustomerOpenId());
        request.setPayer(payer);

        SettleInfo settleInfo = new SettleInfo();
        settleInfo.setProfitSharing(true);
        request.setSettleInfo(settleInfo);
        return request;
    }

    /**
     * 处理支付
     * <p>
     * 该方法用于处理支付成功后的逻辑。它首先检查订单是否已支付，
     * 如果已支付则直接返回，否则更新订单状态，并发送支付成功消息。
     * </p>
     *
     * @param transaction 包含支付信息的对象
     */
    private void handlePayment(Transaction transaction) {
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(
                new LambdaQueryWrapper<PaymentInfo>()
                        .eq(PaymentInfo::getOrderNo, transaction.getOutTradeNo())
        );
        if (paymentInfo.getPaymentStatus() == 1) {
            return;
        }
        paymentInfo.setPaymentStatus(1);
        paymentInfo.setOrderNo(transaction.getOutTradeNo());
        paymentInfo.setTransactionId(transaction.getTransactionId());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(JSON.toJSONString(transaction));
        paymentInfoMapper.updateById(paymentInfo);
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER, MqConst.ROUTING_PAY_SUCCESS, paymentInfo.getOrderNo());
    }
}
