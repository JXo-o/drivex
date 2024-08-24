package com.jxh.drivex.coupon.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.coupon.UseCouponForm;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.coupon.AvailableCouponVo;
import com.jxh.drivex.model.vo.coupon.NoReceiveCouponVo;
import com.jxh.drivex.model.vo.coupon.NoUseCouponVo;
import com.jxh.drivex.model.vo.coupon.UsedCouponVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "优惠券活动接口管理")
@RestController
@RequestMapping(value="/coupon/info")
public class CouponInfoController {

    @Operation(summary = "查询未领取优惠券分页列表")
    @GetMapping("/findNoReceivePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoReceiveCouponVo>> findNoReceivePage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit)
    {
        return Result.ok();
    }

    @Operation(summary = "查询未使用优惠券分页列表")
    @GetMapping("/findNoUsePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoUseCouponVo>> findNoUsePage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit)
    {
        return Result.ok();
    }

    @Operation(summary = "领取优惠券")
    @GetMapping("/receive/{customerId}/{couponId}")
    Result<Boolean> receive(
            @PathVariable("customerId") Long customerId,
            @PathVariable("couponId") Long couponId)
    {
        return Result.ok();
    }

    @Operation(summary = "获取未使用的最佳优惠券信息")
    @GetMapping("/findAvailableCoupon/{customerId}/{orderAmount}")
    Result<List<AvailableCouponVo>> findAvailableCoupon(
            @PathVariable("customerId") Long customerId,
            @PathVariable("orderAmount") BigDecimal orderAmount)
    {
        return Result.ok();
    }

    @Operation(summary = "使用优惠券")
    @PostMapping("/useCoupon")
    Result<BigDecimal> useCoupon(@RequestBody UseCouponForm useCouponForm)
    {
        return Result.ok();
    }

    @Operation(summary = "查询已使用优惠券分页列表")
    @GetMapping("/findUsedPage/{customerId}/{page}/{limit}")
    Result<PageVo<UsedCouponVo>> findUsedPage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    ) {
        return Result.ok();
    }
}

