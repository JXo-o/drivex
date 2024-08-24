package com.jxh.drivex.coupon.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.coupon.UseCouponForm;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.coupon.AvailableCouponVo;
import com.jxh.drivex.model.vo.coupon.NoReceiveCouponVo;
import com.jxh.drivex.model.vo.coupon.NoUseCouponVo;
import com.jxh.drivex.model.vo.coupon.UsedCouponVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(value = "service-coupon", contextId = "coupon")
public interface CouponFeignClient {

    /**
     * 查询未领取优惠券分页列表
     */
    @GetMapping("/coupon/info/findNoReceivePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoReceiveCouponVo>> findNoReceivePage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    );

    /**
     * 查询未使用优惠券分页列表
     */
    @GetMapping("/coupon/info/findNoUsePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoUseCouponVo>> findNoUsePage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    );

    /**
     * 领取优惠券
     */
    @GetMapping("/coupon/info/receive/{customerId}/{couponId}")
    Result<Boolean> receive(
            @PathVariable("customerId") Long customerId,
            @PathVariable("couponId") Long couponId
    );

    /**
     * 获取未使用的最佳优惠券信息
     */
    @GetMapping("/coupon/info/findAvailableCoupon/{customerId}/{orderAmount}")
    Result<List<AvailableCouponVo>> findAvailableCoupon(
            @PathVariable("customerId") Long customerId,
            @PathVariable("orderAmount") BigDecimal orderAmount
    );

    /**
     * 使用优惠券
     */
    @PostMapping("/coupon/info/useCoupon")
    Result<BigDecimal> useCoupon(@RequestBody UseCouponForm useCouponForm);


    /**
     * 查询已使用优惠券分页列表
     */
    @GetMapping("/coupon/info/findUsedPage/{customerId}/{page}/{limit}")
    Result<PageVo<UsedCouponVo>> findUsedPage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit
    );
}