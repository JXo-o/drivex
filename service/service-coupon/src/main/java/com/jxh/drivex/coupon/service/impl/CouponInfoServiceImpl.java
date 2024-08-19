package com.jxh.drivex.coupon.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.coupon.mapper.CouponInfoMapper;
import com.jxh.drivex.coupon.service.CouponInfoService;
import com.jxh.drivex.model.entity.coupon.CouponInfo;
import org.springframework.stereotype.Service;

@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo>
        implements CouponInfoService {

}
