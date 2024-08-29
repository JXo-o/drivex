package com.jxh.drivex.driver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.jxh.drivex.common.constant.SystemConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.common.util.LocationUtil;
import com.jxh.drivex.dispatch.client.NewOrderFeignClient;
import com.jxh.drivex.driver.service.OrderService;
import com.jxh.drivex.map.client.LocationFeignClient;
import com.jxh.drivex.map.client.MapFeignClient;
import com.jxh.drivex.model.entity.order.OrderInfo;
import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.form.order.OrderFeeForm;
import com.jxh.drivex.model.form.order.StartDriveForm;
import com.jxh.drivex.model.form.order.UpdateOrderBillForm;
import com.jxh.drivex.model.form.order.UpdateOrderCartForm;
import com.jxh.drivex.model.form.rules.FeeRuleRequestForm;
import com.jxh.drivex.model.form.rules.ProfitsharingRuleRequestForm;
import com.jxh.drivex.model.form.rules.RewardRuleRequestForm;
import com.jxh.drivex.model.vo.map.DrivingLineVo;
import com.jxh.drivex.model.vo.map.OrderLocationVo;
import com.jxh.drivex.model.vo.map.OrderServiceLastLocationVo;
import com.jxh.drivex.model.vo.order.CurrentOrderInfoVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import com.jxh.drivex.model.vo.order.OrderInfoVo;
import com.jxh.drivex.model.vo.rules.FeeRuleResponseVo;
import com.jxh.drivex.model.vo.rules.ProfitsharingRuleResponseVo;
import com.jxh.drivex.model.vo.rules.RewardRuleResponseVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import com.jxh.drivex.rules.client.FeeRuleFeignClient;
import com.jxh.drivex.rules.client.ProfitsharingRuleFeignClient;
import com.jxh.drivex.rules.client.RewardRuleFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderInfoFeignClient orderInfoFeignClient;
    private final NewOrderFeignClient newOrderFeignClient;
    private final MapFeignClient mapFeignClient;
    private final FeeRuleFeignClient feeRuleFeignClient;
    private final ProfitsharingRuleFeignClient profitsharingRuleFeignClient;
    private final LocationFeignClient locationFeignClient;
    private final RewardRuleFeignClient rewardRuleFeignClient;

    public OrderServiceImpl(
            OrderInfoFeignClient orderInfoFeignClient,
            NewOrderFeignClient newOrderFeignClient,
            MapFeignClient mapFeignClient,
            FeeRuleFeignClient feeRuleFeignClient,
            ProfitsharingRuleFeignClient profitsharingRuleFeignClient,
            LocationFeignClient locationFeignClient,
            RewardRuleFeignClient rewardRuleFeignClient
    ) {
        this.orderInfoFeignClient = orderInfoFeignClient;
        this.newOrderFeignClient = newOrderFeignClient;
        this.mapFeignClient = mapFeignClient;
        this.feeRuleFeignClient = feeRuleFeignClient;
        this.profitsharingRuleFeignClient = profitsharingRuleFeignClient;
        this.locationFeignClient = locationFeignClient;
        this.rewardRuleFeignClient = rewardRuleFeignClient;
    }

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }

    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        return newOrderFeignClient.findNewOrderQueueData(driverId).getData();
    }

    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        return orderInfoFeignClient.robNewOrder(driverId, orderId).getData();
    }

    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        return orderInfoFeignClient.searchDriverCurrentOrder(driverId).getData();
    }

    @Override
    public OrderInfoVo getOrderInfo(Long orderId, Long driverId) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if(!orderInfo.getDriverId().equals(driverId)) {
            throw new DrivexException(ResultCodeEnum.ORDER_ID_NOT_FOUND);
        }
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setOrderId(orderId);
        BeanUtils.copyProperties(orderInfo, orderInfoVo);
        return orderInfoVo;
    }

    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        return mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
    }

    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        OrderLocationVo orderLocationVo = locationFeignClient.getCacheOrderLocation(orderId).getData();
        double distance = LocationUtil.getDistance(
                orderInfo.getStartPointLatitude().doubleValue(),
                orderInfo.getStartPointLongitude().doubleValue(),
                orderLocationVo.getLatitude().doubleValue(),
                orderLocationVo.getLongitude().doubleValue()
        );
        if(distance > SystemConstant.DRIVER_START_LOCATION_DISTANCE) {
            throw new DrivexException(ResultCodeEnum.START_LOCATION_DISTANCE_ERROR);
        }
        return orderInfoFeignClient.driverArriveStartLocation(orderId, driverId).getData();
    }

    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        return orderInfoFeignClient.updateOrderCart(updateOrderCartForm).getData();
    }

    @Override
    public Boolean startDrive(StartDriveForm startDriveForm) {
        return orderInfoFeignClient.startDrive(startDriveForm).getData();
    }

    /**
     * 结束代驾
     * <ol>
     *     <li>计算订单实际里程</li>
     *     <li>计算代驾实际费用</li>
     *     <li>计算系统奖励</li>
     *     <li>计算分账信息</li>
     *     <li>封装更新订单账单相关实体对象</li>
     *     <li>结束代驾更新账单</li>
     * </ol>
     *
     * @param orderFeeForm 订单费用表单
     * @return 是否成功
     */
    @Override
    public Boolean endDrive(OrderFeeForm orderFeeForm) {
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderFeeForm.getOrderId()).getData();
        if(!orderInfo.getDriverId().equals(orderFeeForm.getDriverId())) {
            throw new DrivexException(ResultCodeEnum.ORDER_ID_NOT_FOUND);
        }

        OrderServiceLastLocationVo orderServiceLastLocationVo =
                locationFeignClient.getOrderServiceLastLocation(orderFeeForm.getOrderId()).getData();
        double distance = LocationUtil.getDistance(
                orderInfo.getEndPointLatitude().doubleValue(),
                orderInfo.getEndPointLongitude().doubleValue(),
                orderServiceLastLocationVo.getLatitude().doubleValue(),
                orderServiceLastLocationVo.getLongitude().doubleValue()
        );
        if(distance > SystemConstant.DRIVER_START_LOCATION_DISTANCE) {
            throw new DrivexException(ResultCodeEnum.END_LOCATION_DISTANCE_ERROR);
        }

        BigDecimal realDistance = locationFeignClient.calculateOrderRealDistance(orderFeeForm.getOrderId()).getData();
        log.info("结束代驾，订单实际里程：{}", realDistance);

        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDistance(realDistance);
        feeRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
        Integer waitMinute = Math.abs((int) ((orderInfo.getArriveTime().getTime() - orderInfo.getAcceptTime().getTime()) / (1000 * 60)));
        feeRuleRequestForm.setWaitMinute(waitMinute);
        log.info("结束代驾，费用参数：{}", JSON.toJSONString(feeRuleRequestForm));
        FeeRuleResponseVo feeRuleResponseVo = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();
        log.info("费用明细：{}", JSON.toJSONString(feeRuleResponseVo));
        BigDecimal totalAmount = feeRuleResponseVo.getTotalAmount().add(orderFeeForm.getTollFee()).add(orderFeeForm.getParkingFee()).add(orderFeeForm.getOtherFee()).add(orderInfo.getFavourFee());
        feeRuleResponseVo.setTotalAmount(totalAmount);

        String startTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 00:00:00";
        String endTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 24:00:00";
        Long orderNum = orderInfoFeignClient.getOrderNumByTime(startTime, endTime).getData();
        RewardRuleRequestForm rewardRuleRequestForm = new RewardRuleRequestForm();
        rewardRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
        rewardRuleRequestForm.setOrderNum(orderNum);
        RewardRuleResponseVo rewardRuleResponseVo = rewardRuleFeignClient.calculateOrderRewardFee(rewardRuleRequestForm).getData();
        log.info("结束代驾，系统奖励：{}", JSON.toJSONString(rewardRuleResponseVo));

        ProfitsharingRuleRequestForm profitsharingRuleRequestForm = new ProfitsharingRuleRequestForm();
        profitsharingRuleRequestForm.setOrderAmount(feeRuleResponseVo.getTotalAmount());
        profitsharingRuleRequestForm.setOrderNum(orderNum);
        ProfitsharingRuleResponseVo profitsharingRuleResponseVo = profitsharingRuleFeignClient.calculateOrderProfitsharingFee(profitsharingRuleRequestForm).getData();
        log.info("结束代驾，分账信息：{}", JSON.toJSONString(profitsharingRuleResponseVo));

        UpdateOrderBillForm updateOrderBillForm = new UpdateOrderBillForm();
        updateOrderBillForm.setOrderId(orderFeeForm.getOrderId());
        updateOrderBillForm.setDriverId(orderFeeForm.getDriverId());
        updateOrderBillForm.setTollFee(orderFeeForm.getTollFee());
        updateOrderBillForm.setParkingFee(orderFeeForm.getParkingFee());
        updateOrderBillForm.setOtherFee(orderFeeForm.getOtherFee());
        updateOrderBillForm.setFavourFee(orderInfo.getFavourFee());

        updateOrderBillForm.setRealDistance(realDistance);
        BeanUtils.copyProperties(rewardRuleResponseVo, updateOrderBillForm);
        BeanUtils.copyProperties(feeRuleResponseVo, updateOrderBillForm);

        BeanUtils.copyProperties(profitsharingRuleResponseVo, updateOrderBillForm);
        updateOrderBillForm.setProfitsharingRuleId(profitsharingRuleResponseVo.getProfitsharingRuleId());
        log.info("结束代驾，更新账单信息：{}", JSON.toJSONString(updateOrderBillForm));
        orderInfoFeignClient.endDrive(updateOrderBillForm);
        return true;
    }

}
