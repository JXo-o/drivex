package com.jxh.drivex.payment.service;

import com.jxh.drivex.model.form.payment.PaymentInfoForm;
import com.jxh.drivex.model.vo.payment.WxPrepayVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface WxPayService {

    WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm);

    Map<String,Object> wxNotify(HttpServletRequest request);

    Boolean queryPayStatus(String orderNo);

    void handleOrder(String orderNo);
}
