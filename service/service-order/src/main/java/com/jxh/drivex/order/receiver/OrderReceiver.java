package com.jxh.drivex.order.receiver;

import com.jxh.drivex.common.constant.MqConst;
import com.jxh.drivex.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ClassName: OrderReceiver
 * Package: com.jxh.drivex.order.receiver
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/30 16:56
 */
@Slf4j
@Component
public class OrderReceiver {

    private final OrderInfoService orderInfoService;

    public OrderReceiver(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    /**
     * 订单微服务关闭订单
     * @param orderId 订单id
     * @param message 消息
     * @param channel 通道
     */
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_CANCEL_ORDER)
    public void systemCancelOrder(String orderId, Message message, Channel channel) {
        if (orderId != null) {
            log.info("订单微服务关闭订单消息：{}", orderId);
            orderInfoService.systemCancelOrder(Long.parseLong(orderId));
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 订单分账成功，更新分账状态
     *
     * @param orderNo 订单号
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PROFITSHARING_SUCCESS, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER),
            key = {MqConst.ROUTING_PROFITSHARING_SUCCESS}
    ))
    public void profitsharingSuccess(String orderNo, Message message, Channel channel) {
        orderInfoService.updateProfitsharingStatus(orderNo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}