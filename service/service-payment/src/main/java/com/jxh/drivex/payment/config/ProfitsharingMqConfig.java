package com.jxh.drivex.payment.config;

import com.jxh.drivex.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: ProfitsharingMqConfig
 * Package: com.jxh.drivex.payment.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/9/1 16:12
 */
public class ProfitsharingMqConfig {

    @Bean
    public Queue profitsharingQueue() {
        return new Queue(MqConst.QUEUE_PROFITSHARING, true);
    }

    @Bean
    public CustomExchange profitsharingExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(
                MqConst.EXCHANGE_PROFITSHARING,
                "x-delayed-message",
                true,
                false,
                args
        );
    }

    @Bean
    public Binding bindingCancel() {
        return BindingBuilder
                .bind(profitsharingQueue())
                .to(profitsharingExchange())
                .with(MqConst.ROUTING_PROFITSHARING)
                .noargs();
    }

}