package com.jxh.drivex.payment.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.payment.PaymentInfoForm;
import com.jxh.drivex.model.vo.payment.WxPrepayVo;
import com.jxh.drivex.payment.service.WxPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "微信支付接口")
@RestController
@RequestMapping("payment/wxPay")
@Slf4j
public class WxPayController {

    private final WxPayService wxPayService;

    public WxPayController(WxPayService wxPayService) {
        this.wxPayService = wxPayService;
    }

    @Operation(summary = "创建微信支付")
    @PostMapping("/createWxPayment")
    Result<WxPrepayVo> createWxPayment(@RequestBody PaymentInfoForm paymentInfoForm) {
        return Result.ok(wxPayService.createWxPayment(paymentInfoForm));
    }

    @Operation(summary = "微信支付异步通知接口")
    @PostMapping("/notify")
    public Map<String,Object> notify(HttpServletRequest request) {
        return wxPayService.wxNotify(request);
    }

    @Operation(summary = "支付状态查询")
    @GetMapping("/queryPayStatus/{orderNo}")
    Result<Boolean> queryPayStatus(@PathVariable("orderNo") String orderNo) {
        return Result.ok(wxPayService.queryPayStatus(orderNo));
    }
}
