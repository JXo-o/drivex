package com.jxh.drivex.dispatch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.dispatch.mapper.OrderJobMapper;
import com.jxh.drivex.dispatch.service.NewOrderService;
import com.jxh.drivex.dispatch.xxl.client.XxlJobClient;
import com.jxh.drivex.map.client.LocationFeignClient;
import com.jxh.drivex.model.entity.dispatch.OrderJob;
import com.jxh.drivex.model.enums.OrderStatus;
import com.jxh.drivex.model.form.map.SearchNearByDriverForm;
import com.jxh.drivex.model.vo.dispatch.NewOrderTaskVo;
import com.jxh.drivex.model.vo.map.NearByDriverVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;
import com.jxh.drivex.order.client.OrderInfoFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NewOrderServiceImpl implements NewOrderService {

    private final XxlJobClient xxlJobClient;
    private final OrderJobMapper orderJobMapper;
    private final OrderInfoFeignClient orderInfoFeignClient;
    private final LocationFeignClient locationFeignClient;
    private final RedisTemplate<String, String> redisTemplate;

    public NewOrderServiceImpl(
            XxlJobClient xxlJobClient,
            OrderJobMapper orderJobMapper,
            OrderInfoFeignClient orderInfoFeignClient,
            LocationFeignClient locationFeignClient,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.xxlJobClient = xxlJobClient;
        this.orderJobMapper = orderJobMapper;
        this.orderInfoFeignClient = orderInfoFeignClient;
        this.locationFeignClient = locationFeignClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 增加并启动新订单任务。
     *
     * @param newOrderTaskVo 新订单任务的参数封装对象
     * @return 创建或获取的任务ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAndStartTask(NewOrderTaskVo newOrderTaskVo) {
        OrderJob orderJob = orderJobMapper.selectOne(
                new LambdaQueryWrapper<OrderJob>().eq(OrderJob::getOrderId, newOrderTaskVo.getOrderId())
        );

        OrderJob orderJobToUse = Optional.ofNullable(orderJob).orElseGet(() -> {
            Long jobId = xxlJobClient.addAndStart(
                    "newOrderTaskHandler",
                    "",
                    "0 0/1 * * * ?",
                    "新订单任务,订单id:" + newOrderTaskVo.getOrderId()
            );

            OrderJob newOrderJob = new OrderJob();
            newOrderJob.setOrderId(newOrderTaskVo.getOrderId());
            newOrderJob.setJobId(jobId);
            newOrderJob.setParameter(JSONObject.toJSONString(newOrderTaskVo));
            orderJobMapper.insert(newOrderJob);
            return newOrderJob;
        });
        return orderJobToUse.getJobId();
    }

    /**
     * 执行新订单任务。
     * <ol>
     *   <li>获取任务参数：根据任务ID从数据库中查询对应的任务信息，并解析为NewOrderTaskVo对象。</li>
     *   <li>查询订单状态：通过Feign客户端查询订单的当前状态。如果订单已不在接单状态，停止任务调度。</li>
     *   <li>搜索附近司机：根据订单的起点经纬度和期望距离，搜索附近满足条件的司机列表。</li>
     *   <li>过滤司机列表：遍历司机列表，将司机ID放入Redis的Set中，用于过滤重复的司机。</li>
     *   <li>派发订单信息：遍历附近司机列表，将新订单信息推送到每个司机的临时队列中，并设置过期时间。</li>
     * </ol>
     *
     * @param jobId 任务ID
     */
    @Override
    public void executeTask(long jobId) {
        OrderJob orderJob = orderJobMapper.selectOne(
                new LambdaQueryWrapper<OrderJob>().eq(OrderJob::getJobId, jobId)
        );
        if(orderJob == null) {
            return;
        }
        NewOrderTaskVo newOrderTaskVo = JSONObject.parseObject(orderJob.getParameter(), NewOrderTaskVo.class);
        Integer orderStatus = orderInfoFeignClient.getOrderStatus(newOrderTaskVo.getOrderId()).getData();
        if(orderStatus.intValue() != OrderStatus.WAITING_ACCEPT.getStatus().intValue()) {
            xxlJobClient.stopJob(jobId);
            log.info("停止任务调度: {}", JSON.toJSONString(newOrderTaskVo));
            return;
        }

        SearchNearByDriverForm searchNearByDriverForm = new SearchNearByDriverForm();
        searchNearByDriverForm.setLongitude(newOrderTaskVo.getStartPointLongitude());
        searchNearByDriverForm.setLatitude(newOrderTaskVo.getStartPointLatitude());
        searchNearByDriverForm.setMileageDistance(newOrderTaskVo.getExpectDistance());
        List<NearByDriverVo> nearByDriverVoList = locationFeignClient.searchNearByDriver(searchNearByDriverForm).getData();
        nearByDriverVoList.forEach(driver -> {
            String repeatKey = RedisConstant.DRIVER_ORDER_REPEAT_LIST + newOrderTaskVo.getOrderId();
            boolean isMember = Boolean.TRUE.equals(
                    redisTemplate.opsForSet().isMember(repeatKey, driver.getDriverId().toString())
            );
            if(!isMember) {
                redisTemplate.opsForSet().add(repeatKey, driver.getDriverId().toString());
                redisTemplate.expire(repeatKey, RedisConstant.DRIVER_ORDER_REPEAT_LIST_EXPIRES_TIME, TimeUnit.MINUTES);

                NewOrderDataVo newOrderDataVo = new NewOrderDataVo();
                BeanUtils.copyProperties(newOrderTaskVo, newOrderDataVo);
                newOrderDataVo.setDistance(driver.getDistance());

                String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driver.getDriverId();
                redisTemplate.opsForList().leftPush(key, JSONObject.toJSONString(newOrderDataVo));
                redisTemplate.expire(key, RedisConstant.DRIVER_ORDER_TEMP_LIST_EXPIRES_TIME, TimeUnit.MINUTES);
                log.info("该新订单信息已放入司机临时队列: {}", JSON.toJSONString(newOrderDataVo));
            }
        });
    }

    /**
     * 查询司机临时队列中新订单数据。
     *
     * @param driverId 司机ID
     * @return 新订单数据列表
     */
    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        List<NewOrderDataVo> list = new ArrayList<>();
        String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driverId;
        long size = Optional.ofNullable(redisTemplate.opsForList().size(key)).orElse(0L);
        if(size > 0) {
            for(int i = 0; i < size; i++) {
                String content = redisTemplate.opsForList().leftPop(key);
                NewOrderDataVo newOrderDataVo = JSONObject.parseObject(content, NewOrderDataVo.class);
                list.add(newOrderDataVo);
            }
        }
        return list;
    }

    /**
     * 清空司机临时队列中新订单数据。
     * @param driverId 司机ID
     * @return 是否清空成功
     */
    @Override
    public Boolean clearNewOrderQueueData(Long driverId) {
        String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driverId;
        redisTemplate.delete(key);
        return true;
    }
}
