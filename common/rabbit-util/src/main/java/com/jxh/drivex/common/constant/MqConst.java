package com.jxh.drivex.common.constant;

public class MqConst {

    public static final String EXCHANGE_ORDER = "drivex.order";
    public static final String ROUTING_PAY_SUCCESS = "drivex.pay.success";
    public static final String ROUTING_PROFITSHARING_SUCCESS = "drivex.profitsharing.success";
    public static final String QUEUE_PAY_SUCCESS = "drivex.pay.success";
    public static final String QUEUE_PROFITSHARING_SUCCESS = "drivex.profitsharing.success";


    //取消订单延迟消息
    public static final String EXCHANGE_CANCEL_ORDER = "drivex.cancel.order";
    public static final String ROUTING_CANCEL_ORDER = "drivex.cancel.order";
    public static final String QUEUE_CANCEL_ORDER = "drivex.cancel.order";

    //分账延迟消息
    public static final String EXCHANGE_PROFITSHARING = "drivex.profitsharing";
    public static final String ROUTING_PROFITSHARING = "drivex.profitsharing";
    public static final String QUEUE_PROFITSHARING  = "drivex.profitsharing";

}
