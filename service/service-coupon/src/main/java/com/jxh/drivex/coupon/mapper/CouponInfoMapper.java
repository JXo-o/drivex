package com.jxh.drivex.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxh.drivex.model.entity.coupon.CouponInfo;
import com.jxh.drivex.model.vo.coupon.NoReceiveCouponVo;
import com.jxh.drivex.model.vo.coupon.NoUseCouponVo;
import com.jxh.drivex.model.vo.coupon.UsedCouponVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    Page<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, Long customerId);

    Page<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, Long customerId);

    Page<UsedCouponVo> findUsedPage(Page<CouponInfo> pageParam, Long customerId);

    Integer updateReceiveCount(Long couponId);

    Integer updateReceiveCountByLimit(Long couponId);

    List<NoUseCouponVo> findNoUseList(Long customerId);

    Integer updateUseCount(Long id);
}
