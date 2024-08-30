package com.jxh.drivex.order.config;

import com.jxh.drivex.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: CancelOrderMqConfig
 * Package: com.jxh.drivex.order.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/30 16:11
 */
public class CancelOrderMqConfig {

    @Bean
    public Queue cancelQueue() {
        return new Queue(MqConst.QUEUE_CANCEL_ORDER, true);
    }

    @Bean
    public CustomExchange cancelExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(
                MqConst.EXCHANGE_CANCEL_ORDER,
                "x-delayed-message",
                true,
                false,
                args
        );
    }

    @Bean
    public Binding bindingCancel() {
        return BindingBuilder.bind(cancelQueue()).to(cancelExchange()).with(MqConst.ROUTING_CANCEL_ORDER).noargs();
    }
}
