package com.jxh.drivex.customer.service.impl;

import com.jxh.drivex.coupon.client.CouponFeignClient;
import com.jxh.drivex.customer.service.CouponService;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.coupon.AvailableCouponVo;
import com.jxh.drivex.model.vo.coupon.NoReceiveCouponVo;
import com.jxh.drivex.model.vo.coupon.NoUseCouponVo;
import com.jxh.drivex.model.vo.coupon.UsedCouponVo;
import com.jxh.drivex.model.vo.order.OrderBillVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    private final CouponFeignClient couponFeignClient;
    private final OrderInfoFeignClient orderInfoFeignClient;

    public CouponServiceImpl(
            CouponFeignClient couponFeignClient,
            OrderInfoFeignClient orderInfoFeignClient
    ) {
        this.couponFeignClient = couponFeignClient;
        this.orderInfoFeignClient = orderInfoFeignClient;
    }

    @Override
    public PageVo<NoReceiveCouponVo> findNoReceivePage(Long customerId, Long page, Long limit) {
        return couponFeignClient.findNoReceivePage(customerId, page, limit).getData();
    }

    @Override
    public PageVo<NoUseCouponVo> findNoUsePage(Long customerId, Long page, Long limit) {
        return couponFeignClient.findNoUsePage(customerId, page, limit).getData();
    }

    @Override
    public PageVo<UsedCouponVo> findUsedPage(Long customerId, Long page, Long limit) {
        return couponFeignClient.findUsedPage(customerId, page, limit).getData();
    }

    @Override
    public Boolean receive(Long customerId, Long couponId) {
        return couponFeignClient.receive(customerId, couponId).getData();
    }

    @Override
    public List<AvailableCouponVo> findAvailableCoupon(Long customerId, Long orderId) {
        OrderBillVo orderBillVo = orderInfoFeignClient.getOrderBillInfo(orderId).getData();
        return couponFeignClient.findAvailableCoupon(customerId, orderBillVo.getPayAmount()).getData();
    }
}
