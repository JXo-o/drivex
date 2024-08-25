package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.driver.client.DriverInfoFeignClient;
import com.jxh.drivex.driver.service.LocationService;
import com.jxh.drivex.map.client.LocationFeignClient;
import com.jxh.drivex.model.entity.driver.DriverSet;
import com.jxh.drivex.model.form.map.UpdateDriverLocationForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    private final LocationFeignClient locationFeignClient;
    private final DriverInfoFeignClient driverInfoFeignClient;

    public LocationServiceImpl(
            LocationFeignClient locationFeignClient,
            DriverInfoFeignClient driverInfoFeignClient
    ) {
        this.locationFeignClient = locationFeignClient;
        this.driverInfoFeignClient = driverInfoFeignClient;
    }

    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        DriverSet driverSet = driverInfoFeignClient.getDriverSet(updateDriverLocationForm.getDriverId()).getData();
        if(driverSet.getServiceStatus() == 1) {
            return locationFeignClient.updateDriverLocation(updateDriverLocationForm).getData();
        } else {
            throw new DrivexException(ResultCodeEnum.SERVICE_NOT_STARTED);
        }
    }
}
