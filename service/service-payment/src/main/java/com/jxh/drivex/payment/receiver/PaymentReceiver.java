package com.jxh.drivex.payment.receiver;

import com.alibaba.fastjson2.JSONObject;
import com.jxh.drivex.common.constant.MqConst;
import com.jxh.drivex.model.form.payment.ProfitsharingForm;
import com.jxh.drivex.payment.service.WxPayService;
import com.jxh.drivex.payment.service.WxProfitsharingService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ClassName: PaymentReceiver
 * Package: com.jxh.drivex.payment.receiver
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/9/1 15:50
 */
@Slf4j
@Component
public class PaymentReceiver {

    private final WxPayService wxPayService;
    private final WxProfitsharingService wxProfitsharingService;

    public PaymentReceiver(
            WxPayService wxPayService,
            WxProfitsharingService wxProfitsharingService
    ) {
        this.wxPayService = wxPayService;
        this.wxProfitsharingService = wxProfitsharingService;
    }

    /**
     * 订单支付成功，处理支付回调
     *
     * @param orderNo 订单号
     * @param message 消息
     * @param channel 通道
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAY_SUCCESS, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER),
            key = {MqConst.ROUTING_PAY_SUCCESS}
    ))
    public void paySuccess(String orderNo, Message message, Channel channel) {
        wxPayService.handleOrder(orderNo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 分账消息
     *
     * @param param  分账参数
     */
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_PROFITSHARING)
    public void profitsharingMessage(String param, Message message, Channel channel) {
        try {
            ProfitsharingForm profitsharingForm = JSONObject.parseObject(param, ProfitsharingForm.class);
            log.info("分账：{}", param);
            wxProfitsharingService.profitsharing(profitsharingForm);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.info("分账调用失败：{}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
