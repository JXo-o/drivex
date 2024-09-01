package com.jxh.drivex.customer.service;

import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.coupon.AvailableCouponVo;
import com.jxh.drivex.model.vo.coupon.NoReceiveCouponVo;
import com.jxh.drivex.model.vo.coupon.NoUseCouponVo;
import com.jxh.drivex.model.vo.coupon.UsedCouponVo;

import java.util.List;

public interface CouponService  {

    PageVo<NoReceiveCouponVo> findNoReceivePage(Long customerId, Long page, Long limit);

    PageVo<NoUseCouponVo> findNoUsePage(Long customerId, Long page, Long limit);

    PageVo<UsedCouponVo> findUsedPage(Long customerId, Long page, Long limit);

    Boolean receive(Long customerId, Long couponId);

    List<AvailableCouponVo> findAvailableCoupon(Long customerId, Long orderId);
}
