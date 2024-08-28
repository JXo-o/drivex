package com.jxh.drivex.driver.controller;

import com.jxh.drivex.common.login.DrivexLogin;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.common.util.AuthContextHolder;
import com.jxh.drivex.driver.service.LocationService;
import com.jxh.drivex.model.form.map.OrderServiceLocationForm;
import com.jxh.drivex.model.form.map.UpdateDriverLocationForm;
import com.jxh.drivex.model.form.map.UpdateOrderLocationForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "位置API接口管理")
@RestController
@RequestMapping(value="/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @DrivexLogin
    @Operation(summary = "开启接单服务：更新司机经纬度位置")
    @PostMapping("/updateDriverLocation")
    public Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm) {
        Long driverId = AuthContextHolder.getUserId();
        updateDriverLocationForm.setDriverId(driverId);
        return Result.ok(locationService.updateDriverLocation(updateDriverLocationForm));
    }

    @DrivexLogin
    @Operation(summary = "司机赶往代驾起始点：更新订单位置到Redis缓存")
    @PostMapping("/updateOrderLocationToCache")
    public Result<Boolean> updateOrderLocationToCache(@RequestBody UpdateOrderLocationForm updateOrderLocationForm) {
        return Result.ok(locationService.updateOrderLocationToCache(updateOrderLocationForm));
    }

    @DrivexLogin
    @Operation(summary = "开始代驾服务：保存代驾服务订单位置")
    @PostMapping("/saveOrderServiceLocation")
    public Result<Boolean> saveOrderServiceLocation(
            @RequestBody List<OrderServiceLocationForm> orderLocationServiceFormList
    ) {
        return Result.ok(locationService.saveOrderServiceLocation(orderLocationServiceFormList));
    }

}

