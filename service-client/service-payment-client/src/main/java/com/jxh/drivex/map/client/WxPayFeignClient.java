package com.jxh.drivex.map.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.payment.PaymentInfoForm;
import com.jxh.drivex.model.vo.payment.WxPrepayVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-payment", contextId = "wxPay")
public interface WxPayFeignClient {

    /**
     * 创建微信支付
     */
    @PostMapping("/payment/wxPay/createWxPayment")
    Result<WxPrepayVo> createWxPayment(@RequestBody PaymentInfoForm paymentInfoForm);

    /**
     * 支付状态查询
     */
    @GetMapping("/payment/wxPay/queryPayStatus/{orderNo}")
    Result<Boolean> queryPayStatus(@PathVariable("orderNo") String orderNo);
}