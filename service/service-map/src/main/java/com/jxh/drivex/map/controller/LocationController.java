package com.jxh.drivex.map.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.map.service.LocationService;
import com.jxh.drivex.model.form.map.OrderServiceLocationForm;
import com.jxh.drivex.model.form.map.SearchNearByDriverForm;
import com.jxh.drivex.model.form.map.UpdateDriverLocationForm;
import com.jxh.drivex.model.form.map.UpdateOrderLocationForm;
import com.jxh.drivex.model.vo.map.NearByDriverVo;
import com.jxh.drivex.model.vo.map.OrderLocationVo;
import com.jxh.drivex.model.vo.map.OrderServiceLastLocationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Tag(name = "位置API接口管理")
@RestController
@RequestMapping("/map/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "开启接单服务：更新司机经纬度位置")
    @PostMapping("/updateDriverLocation")
    Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm) {
        return Result.ok(locationService.updateDriverLocation(updateDriverLocationForm));
    }

    @Operation(summary = "关闭接单服务：删除司机经纬度位置")
    @DeleteMapping("/removeDriverLocation/{driverId}")
    Result<Boolean> removeDriverLocation(@PathVariable("driverId") Long driverId) {
        return Result.ok(locationService.removeDriverLocation(driverId));
    }

    @Operation(summary = "搜索附近满足条件的司机")
    @PostMapping("/searchNearByDriver")
    Result<List<NearByDriverVo>> searchNearByDriver(@RequestBody SearchNearByDriverForm searchNearByDriverForm) {
        return Result.ok(locationService.searchNearByDriver(searchNearByDriverForm));
    }

    @Operation(summary = "司机赶往代驾起始点：更新订单地址到缓存")
    @PostMapping("/updateOrderLocationToCache")
    Result<Boolean> updateOrderLocationToCache(@RequestBody UpdateOrderLocationForm updateOrderLocationForm) {
        return Result.ok(locationService.updateOrderLocationToCache(updateOrderLocationForm));
    }

    @Operation(summary = "司机赶往代驾起始点：获取订单经纬度位置")
    @GetMapping("/getCacheOrderLocation/{orderId}")
    Result<OrderLocationVo> getCacheOrderLocation(@PathVariable("orderId") Long orderId) {
        return Result.ok(locationService.getCacheOrderLocation(orderId));
    }

    @Operation(summary = "开始代驾服务：保存代驾服务订单位置")
    @PostMapping("/saveOrderServiceLocation")
    Result<Boolean> saveOrderServiceLocation(@RequestBody List<OrderServiceLocationForm> orderLocationServiceFormList) {
        return Result.ok(locationService.saveOrderServiceLocation(orderLocationServiceFormList));
    }

    @Operation(summary = "代驾服务：获取订单服务最后一个位置信息")
    @GetMapping("/getOrderServiceLastLocation/{orderId}")
    Result<OrderServiceLastLocationVo> getOrderServiceLastLocation(@PathVariable("orderId") Long orderId) {
        return Result.ok(locationService.getOrderServiceLastLocation(orderId));
    }

    @Operation(summary = "代驾服务：计算订单实际里程")
    @GetMapping("/calculateOrderRealDistance/{orderId}")
    Result<BigDecimal> calculateOrderRealDistance(@PathVariable("orderId") Long orderId) {
        return Result.ok(locationService.calculateOrderRealDistance(orderId));
    }

}

