package com.jxh.drivex.common.config;

import com.alibaba.fastjson2.JSON;
import com.jxh.drivex.common.entity.DrivexCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: RabbitInitConfigApplicationListener
 * Package: com.jxh.drivex.common.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/30 15:43
 */
@Slf4j
@Component
public class RabbitInitConfigApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public RabbitInitConfigApplicationListener(
            RabbitTemplate rabbitTemplate,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent applicationReadyEvent) {
        this.setupCallbacks();
    }

    /**
     * 设置回调
     */
    private void setupCallbacks() {


        /*
         * 只确认消息是否正确到达 Exchange 中,成功与否都会回调
         *
         * @param correlation 相关数据  非消息本身业务数据
         * @param ack             应答结果
         * @param reason           如果发送消息到交换器失败，错误原因
         */
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, reason) -> {
            if (ack) {
                log.info("消息发送到Exchange成功：{}", correlationData);
            } else {
                log.error("消息发送到Exchange失败：{}", reason);
                this.retrySendMsg(correlationData);
            }
        });

        /*
         * 消息没有正确到达队列时触发回调，如果正确到达队列不执行，不考虑延迟消息重发 直接返回
         */
        this.rabbitTemplate.setReturnsCallback(returned -> {
            log.error("Returned: " + returned.getMessage() + "\nreplyCode: " + returned.getReplyCode()
                    + "\nreplyText: " + returned.getReplyText() + "\nexchange/rk: "
                    + returned.getExchange() + "/" + returned.getRoutingKey());
            String redisKey = returned.getMessage().getMessageProperties()
                    .getHeader("spring_returned_message_correlation");
            String correlationDataStr = redisTemplate.opsForValue().get(redisKey);
            DrivexCorrelationData drivexCorrelationData = JSON
                    .parseObject(correlationDataStr, DrivexCorrelationData.class);
            if(Objects.requireNonNull(drivexCorrelationData).isDelay()){
                return;
            }
            this.retrySendMsg(drivexCorrelationData);
        });
    }


    /**
     * 消息重新发送，区分正常消息和延迟消息
     *
     * @param correlationData 相关数据
     */
    private void retrySendMsg(CorrelationData correlationData) {
        DrivexCorrelationData drivexCorrelationData = (DrivexCorrelationData) correlationData;
        if (drivexCorrelationData.getRetryCount() >= 3) {
            log.error("生产者超过最大重试次数，将失败的消息存入数据库用人工处理；给管理员发送邮件；给管理员发送短信；");
            return;
        }
        rabbitTemplate.convertAndSend(
                drivexCorrelationData.getExchange(),
                drivexCorrelationData.getRoutingKey(),
                drivexCorrelationData.getMessage(),
                drivexCorrelationData
        );
        drivexCorrelationData.retryCountAdd();
        redisTemplate.opsForValue().set(
                drivexCorrelationData.getId(),
                JSON.toJSONString(drivexCorrelationData),
                10,
                TimeUnit.MINUTES
        );
        log.info("进行消息重发！");

        if (drivexCorrelationData.isDelay()) {
            rabbitTemplate.convertAndSend(
                    drivexCorrelationData.getExchange(),
                    drivexCorrelationData.getRoutingKey(),
                    drivexCorrelationData.getMessage(), message -> {
                        message.getMessageProperties().setDelay(drivexCorrelationData.getDelayTime() * 1000);
                        return message;
                    },
                    drivexCorrelationData
            );
        } else {
            rabbitTemplate.convertAndSend(
                    drivexCorrelationData.getExchange(),
                    drivexCorrelationData.getRoutingKey(),
                    drivexCorrelationData.getMessage(),
                    drivexCorrelationData
            );
        }
    }

}