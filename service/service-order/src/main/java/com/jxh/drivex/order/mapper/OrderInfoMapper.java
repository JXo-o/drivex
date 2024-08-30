package com.jxh.drivex.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.vo.order.OrderListVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    Page<OrderListVo> selectCustomerOrderPage(Page<OrderInfo> pageParam, Long customerId);

    Page<OrderListVo> selectDriverOrderPage(Page<OrderInfo> pageParam, Long driverId);
}
