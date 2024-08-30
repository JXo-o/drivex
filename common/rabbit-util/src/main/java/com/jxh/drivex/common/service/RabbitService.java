package com.jxh.drivex.common.service;

import com.alibaba.fastjson2.JSON;
import com.jxh.drivex.common.entity.DrivexCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RabbitService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public RabbitService(
            RabbitTemplate rabbitTemplate,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发送消息
     * <p>
     * 1.创建自定义相关消息对象-包含业务数据本身，交换器名称，路由键，队列类型，延迟时间,重试次数
     * 2.将相关消息封装到发送消息方法中
     * 3.将相关消息存入Redis Key：UUID  相关消息对象 10 分钟
     * </p>
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        DrivexCorrelationData correlationData = new DrivexCorrelationData();
        String uuid = "mq:" + UUID.randomUUID().toString().replaceAll("-", "");
        correlationData.setId(uuid);
        correlationData.setMessage(message);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);

        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
        redisTemplate.opsForValue().set(uuid, JSON.toJSONString(correlationData), 10, TimeUnit.MINUTES);
        log.info("生产者发送消息成功：{}，{}，{}", exchange, routingKey, message);
        return true;
    }

    /**
     * 发送延迟消息方法
     * <p>
     * 1.创建自定义相关消息对象-包含业务数据本身，交换器名称，路由键，队列类型，延迟时间,重试次数
     * 2.将相关消息封装到发送消息方法中
     * 3.将相关消息存入Redis Key：UUID 相关消息对象 10 分钟
     * <p/>
     *
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息数据
     * @param delayTime 延迟时间，单位为：秒
     */
    public boolean sendDelayMessage(String exchange, String routingKey, Object message, int delayTime) {
        DrivexCorrelationData correlationData = new DrivexCorrelationData();
        String uuid = "mq:" + UUID.randomUUID().toString().replaceAll("-", "");
        correlationData.setId(uuid);
        correlationData.setMessage(message);
        correlationData.setExchange(exchange);
        correlationData.setRoutingKey(routingKey);
        correlationData.setDelay(true);
        correlationData.setDelayTime(delayTime);

        rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
            message1.getMessageProperties().setDelay(delayTime * 1000);
            return message1;
        }, correlationData);
        redisTemplate.opsForValue().set(uuid, JSON.toJSONString(correlationData), 10, TimeUnit.MINUTES);
        return true;
    }
}
