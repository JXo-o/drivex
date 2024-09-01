package com.jxh.drivex.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxh.drivex.model.entity.order.OrderBill;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface OrderBillMapper extends BaseMapper<OrderBill> {

    Integer updateCouponAmount(Long orderId, BigDecimal couponAmount);
}
