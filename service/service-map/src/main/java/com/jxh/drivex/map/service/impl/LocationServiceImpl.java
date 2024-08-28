package com.jxh.drivex.map.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.common.constant.SystemConstant;
import com.jxh.drivex.driver.client.DriverInfoFeignClient;
import com.jxh.drivex.map.repository.OrderServiceLocationRepository;
import com.jxh.drivex.map.service.LocationService;
import com.jxh.drivex.model.entity.driver.DriverSet;
import com.jxh.drivex.model.entity.map.OrderServiceLocation;
import com.jxh.drivex.model.form.map.OrderServiceLocationForm;
import com.jxh.drivex.model.form.map.SearchNearByDriverForm;
import com.jxh.drivex.model.form.map.UpdateDriverLocationForm;
import com.jxh.drivex.model.form.map.UpdateOrderLocationForm;
import com.jxh.drivex.model.vo.map.NearByDriverVo;
import com.jxh.drivex.model.vo.map.OrderLocationVo;
import com.jxh.drivex.model.vo.map.OrderServiceLastLocationVo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DriverInfoFeignClient driverInfoFeignClient;
    private final OrderServiceLocationRepository orderServiceLocationRepository;
    private final MongoTemplate mongoTemplate;

    public LocationServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            DriverInfoFeignClient driverInfoFeignClient,
            OrderServiceLocationRepository orderServiceLocationRepository,
            MongoTemplate mongoTemplate
    ) {
        this.redisTemplate = redisTemplate;
        this.driverInfoFeignClient = driverInfoFeignClient;
        this.orderServiceLocationRepository = orderServiceLocationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 更新指定司机的位置到Redis中。
     * <p>
     * 该方法根据传入的司机位置信息（经度和纬度）更新Redis中的司机位置。
     * </p>
     *
     * @param updateDriverLocationForm 包含司机ID、经度和纬度的表单
     * @return 操作是否成功，返回true表示成功
     */
    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        Point point = new Point(
                updateDriverLocationForm.getLongitude().doubleValue(),
                updateDriverLocationForm.getLatitude().doubleValue()
        );
        redisTemplate.opsForGeo().add(
                RedisConstant.DRIVER_GEO_LOCATION,
                point,
                updateDriverLocationForm.getDriverId().toString()
        );
        return true;
    }

    /**
     * 从Redis中删除指定司机的位置。
     *
     * @param driverId 司机的ID
     * @return 操作是否成功，返回true表示成功
     */
    @Override
    public Boolean removeDriverLocation(Long driverId) {
        redisTemplate.opsForGeo().remove(
                RedisConstant.DRIVER_GEO_LOCATION,
                driverId.toString()
        );
        return true;
    }

    /**
     * 根据用户提供的经纬度，搜索指定半径范围内的司机。
     * <ol>
     *      <li>创建地理位置点和搜索范围。</li>
     *      <li>定义GEO查询参数，包括距离、坐标和排序方式。</li>
     *      <li>执行GEO查询，获取附近司机信息。</li>
     *      <li>遍历查询结果，判断每位司机是否满足接单和订单里程的要求。</li>
     *      <li>返回满足条件的司机信息列表。</li>
     * </ol>
     *
     * @param searchNearByDriverForm 包含经纬度和订单里程等信息的表单
     * @return 符合条件的附近司机信息列表
     */
    @Override
    public List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm) {
        Point point = new Point(searchNearByDriverForm.getLongitude().doubleValue(),
                searchNearByDriverForm.getLatitude().doubleValue());
        Distance distance = new Distance(SystemConstant.NEARBY_DRIVER_RADIUS,
                RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle searchArea = new Circle(point, distance);

        RedisGeoCommands.GeoRadiusCommandArgs geoArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo()
                .radius(RedisConstant.DRIVER_GEO_LOCATION, searchArea, geoArgs);

        List<NearByDriverVo> nearbyDrivers = new ArrayList<>();
        if (geoResults != null && !geoResults.getContent().isEmpty()) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : geoResults) {
                Long driverId = Long.parseLong(result.getContent().getName());
                BigDecimal distanceFromUser = BigDecimal.valueOf(result.getDistance().getValue())
                        .setScale(2, RoundingMode.HALF_UP);
                log.info("司机ID：{}，距离：{}", driverId, distanceFromUser);

                DriverSet driverSettings = driverInfoFeignClient.getDriverSet(driverId).getData();
                if (isDriverEligible(driverSettings, distanceFromUser, searchNearByDriverForm.getMileageDistance())) {
                    NearByDriverVo driverInfo = new NearByDriverVo();
                    driverInfo.setDriverId(driverId);
                    driverInfo.setDistance(distanceFromUser);
                    nearbyDrivers.add(driverInfo);
                }
            }
        }
        return nearbyDrivers;
    }

    /**
     * 司机赶往代驾起始点：更新订单地址到缓存。
     *
     * @param updateOrderLocationForm 订单ID、经度和纬度
     * @return 操作是否成功，返回true表示成功
     */
    @Override
    public Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm) {
        OrderLocationVo orderLocationVo = new OrderLocationVo();
        orderLocationVo.setLongitude(updateOrderLocationForm.getLongitude());
        orderLocationVo.setLatitude(updateOrderLocationForm.getLatitude());
        redisTemplate.opsForValue().set(
                RedisConstant.UPDATE_ORDER_LOCATION + updateOrderLocationForm.getOrderId(),
                JSONObject.toJSONString(orderLocationVo)
        );
        return true;
    }

    /**
     * 司机赶往代驾起始点：获取订单经纬度位置。
     *
     * @param orderId 订单ID
     * @return 订单经纬度位置
     */
    @Override
    public OrderLocationVo getCacheOrderLocation(Long orderId) {
        return JSONObject.parseObject(
                redisTemplate.opsForValue().get(RedisConstant.UPDATE_ORDER_LOCATION + orderId),
                OrderLocationVo.class
        );
    }

    /**
     * 保存代驾服务订单位置，存入mongodb。
     *
     * @param orderLocationServiceFormList 订单位置信息列表
     * @return 操作是否成功，返回true表示成功
     */
    @Override
    public Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList) {
        List<OrderServiceLocation> list = new ArrayList<>();
        orderLocationServiceFormList.forEach(item -> {
            OrderServiceLocation orderServiceLocation = new OrderServiceLocation();
            BeanUtils.copyProperties(item, orderServiceLocation);
            orderServiceLocation.setId(ObjectId.get().toString());
            orderServiceLocation.setCreateTime(new Date());
            list.add(orderServiceLocation);
        });
        orderServiceLocationRepository.saveAll(list);
        return true;
    }

    /**
     * 获取订单服务最后一个位置信息。
     *
     * @param orderId 订单ID
     * @return 订单服务最后一个位置信息
     */
    @Override
    public OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orderId").is(orderId));
        query.with(Sort.by(Sort.Order.desc("createTime")));
        query.limit(1);
        OrderServiceLocation orderServiceLocation = mongoTemplate.findOne(query, OrderServiceLocation.class);
        OrderServiceLastLocationVo orderServiceLastLocationVo = new OrderServiceLastLocationVo();
        BeanUtils.copyProperties(Objects.requireNonNull(orderServiceLocation), orderServiceLastLocationVo);
        return orderServiceLastLocationVo;
    }

    /**
     * 判断司机是否符合接单和订单里程限制。
     *
     * @param driverSettings 司机的接单设置参数
     * @param distanceFromUser 司机与用户的距离
     * @param orderDistance 订单的距离
     * @return 是否符合条件
     */
    private boolean isDriverEligible(DriverSet driverSettings, BigDecimal distanceFromUser, BigDecimal orderDistance) {
        boolean acceptDistanceCheck = driverSettings.getAcceptDistance().doubleValue() == 0
                || driverSettings.getAcceptDistance().compareTo(distanceFromUser) >= 0;
        boolean orderDistanceCheck = driverSettings.getOrderDistance().doubleValue() == 0
                || driverSettings.getOrderDistance().compareTo(orderDistance) >= 0;
        return acceptDistanceCheck && orderDistanceCheck;
    }
}
