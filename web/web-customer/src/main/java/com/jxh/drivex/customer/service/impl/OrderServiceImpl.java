package com.jxh.drivex.customer.service.impl;

import com.jxh.drivex.customer.service.OrderService;
import com.jxh.drivex.dispatch.client.NewOrderFeignClient;
import com.jxh.drivex.map.client.MapFeignClient;
import com.jxh.drivex.model.form.customer.ExpectOrderForm;
import com.jxh.drivex.model.form.customer.SubmitOrderForm;
import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.form.order.OrderInfoForm;
import com.jxh.drivex.model.form.rules.FeeRuleRequestForm;
import com.jxh.drivex.model.vo.customer.ExpectOrderVo;
import com.jxh.drivex.model.vo.dispatch.NewOrderTaskVo;
import com.jxh.drivex.model.vo.map.DrivingLineVo;
import com.jxh.drivex.model.vo.rules.FeeRuleResponseVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import com.jxh.drivex.rules.client.FeeRuleFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final MapFeignClient mapFeignClient;
    private final FeeRuleFeignClient feeRuleFeignClient;
    private final OrderInfoFeignClient orderInfoFeignClient;
    private final NewOrderFeignClient newOrderFeignClient;

    public OrderServiceImpl(
            MapFeignClient mapFeignClient,
            FeeRuleFeignClient feeRuleFeignClient,
            OrderInfoFeignClient orderInfoFeignClient,
            NewOrderFeignClient newOrderFeignClient
    ) {
        this.mapFeignClient = mapFeignClient;
        this.feeRuleFeignClient = feeRuleFeignClient;
        this.orderInfoFeignClient = orderInfoFeignClient;
        this.newOrderFeignClient = newOrderFeignClient;
    }

    /**
     * 预估订单数据。
     *
     * <p>此方法用于预估订单的相关数据，包括计算驾驶线路和订单费用，并将这些数据封装到 `ExpectOrderVo` 对象中。</p>
     *
     * <p>步骤：</p>
     * <ol>
     *     <li>将 `ExpectOrderForm` 转换为 `CalculateDrivingLineForm`。</li>
     *     <li>调用远程服务计算驾驶线路，获取 `DrivingLineVo` 对象。</li>
     *     <li>根据 `DrivingLineVo` 计算订单费用，获取 `FeeRuleResponseVo` 对象。</li>
     *     <li>封装 `DrivingLineVo` 和 `FeeRuleResponseVo` 到 `ExpectOrderVo` 对象中并返回。</li>
     * </ol>
     *
     * @param expectOrderForm 预估订单请求表单，包含起点和终点等信息
     * @return 返回包含驾驶线路和费用规则的 `ExpectOrderVo` 对象
     */
    @Override
    public ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm) {
        CalculateDrivingLineForm calculateDrivingLineForm = new CalculateDrivingLineForm();
        BeanUtils.copyProperties(expectOrderForm, calculateDrivingLineForm);
        DrivingLineVo drivingLineVo = mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
        FeeRuleResponseVo feeRuleResponseVo = this.getFeeRuleResponseVo(drivingLineVo);
        ExpectOrderVo expectOrderVo = new ExpectOrderVo();
        expectOrderVo.setDrivingLineVo(drivingLineVo);
        expectOrderVo.setFeeRuleResponseVo(feeRuleResponseVo);
        return expectOrderVo;
    }

    /**
     * 乘客下单。
     *
     * <p>此方法用于处理乘客下单操作，包括重新计算驾驶线路和订单费用，并将订单信息保存到数据库中。</p>
     *
     * <p>步骤：</p>
     * <ol>
     *     <li>将 `SubmitOrderForm` 转换为 `CalculateDrivingLineForm`。</li>
     *     <li>调用远程服务重新计算驾驶线路，获取 `DrivingLineVo` 对象。</li>
     *     <li>根据 `DrivingLineVo` 重新计算订单费用，获取 `FeeRuleResponseVo` 对象。</li>
     *     <li>封装订单信息到 `OrderInfoForm` 对象中，并设置预期距离和预期金额。</li>
     *     <li>调用远程服务保存订单信息。</li>
     *     <li>添加并执行任务调度，每分钟执行一次，搜索附近司机。</li>
     * </ol>
     *
     * @param submitOrderForm 乘客下单请求表单，包含订单相关信息
     * @return 返回保存的订单 ID
     */
    @Override
    public Long submitOrder(SubmitOrderForm submitOrderForm) {
        CalculateDrivingLineForm calculateDrivingLineForm = new CalculateDrivingLineForm();
        BeanUtils.copyProperties(submitOrderForm, calculateDrivingLineForm);
        DrivingLineVo drivingLineVo = mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
        FeeRuleResponseVo feeRuleResponseVo = this.getFeeRuleResponseVo(drivingLineVo);
        OrderInfoForm orderInfoForm = new OrderInfoForm();
        BeanUtils.copyProperties(submitOrderForm, orderInfoForm);
        orderInfoForm.setExpectDistance(drivingLineVo.getDistance());
        orderInfoForm.setExpectAmount(feeRuleResponseVo.getTotalAmount());
        Long orderId = orderInfoFeignClient.saveOrderInfo(orderInfoForm).getData();

        NewOrderTaskVo newOrderDispatchVo = new NewOrderTaskVo();
        newOrderDispatchVo.setOrderId(orderId);
        BeanUtils.copyProperties(orderInfoForm, newOrderDispatchVo);
        newOrderDispatchVo.setExpectTime(drivingLineVo.getDuration());
        newOrderDispatchVo.setCreateTime(new Date());
        Long jobId = newOrderFeignClient.addAndStartTask(newOrderDispatchVo).getData();
        log.info("订单id为： {}，绑定任务id为：{}", orderId, jobId);
        return orderId;
    }

    /**
     * 获取订单状态。
     *
     * <p>此方法用于根据订单 ID 获取订单的当前状态。</p>
     *
     * @param orderId 订单 ID
     * @return 返回订单的当前状态
     */
    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }

    /**
     * 获取订单费用规则。
     *
     * <p>此方法根据驾驶线路数据计算订单费用规则。</p>
     *
     * @param drivingLineVo 驾驶线路数据对象，包含距离、开始时间等信息
     * @return 返回计算得到的 `FeeRuleResponseVo` 对象
     */
    private FeeRuleResponseVo getFeeRuleResponseVo(DrivingLineVo drivingLineVo) {
        FeeRuleRequestForm calculateOrderFeeForm = new FeeRuleRequestForm();
        calculateOrderFeeForm.setDistance(drivingLineVo.getDistance());
        calculateOrderFeeForm.setStartTime(new Date());
        calculateOrderFeeForm.setWaitMinute(0);
        return feeRuleFeignClient.calculateOrderFee(calculateOrderFeeForm).getData();
    }

}
