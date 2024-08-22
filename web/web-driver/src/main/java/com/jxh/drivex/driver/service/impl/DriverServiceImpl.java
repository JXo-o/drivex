package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.common.login.jwt.JwtUtil;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.driver.client.DriverInfoFeignClient;
import com.jxh.drivex.driver.service.DriverService;
import com.jxh.drivex.model.form.driver.DriverFaceModelForm;
import com.jxh.drivex.model.form.driver.UpdateDriverAuthInfoForm;
import com.jxh.drivex.model.vo.driver.DriverAuthInfoVo;
import com.jxh.drivex.model.vo.driver.DriverLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DriverServiceImpl implements DriverService {

    private final JwtUtil jwtUtil;
    private final DriverInfoFeignClient driverInfoFeignClient;

    public DriverServiceImpl(
            JwtUtil jwtUtil,
            DriverInfoFeignClient driverInfoFeignClient
    ) {
        this.jwtUtil = jwtUtil;
        this.driverInfoFeignClient = driverInfoFeignClient;
    }

    @Override
    public String login(String code) {
        Result<Long> result = driverInfoFeignClient.login(code);
        return jwtUtil.createToken(result.getData());
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        return driverInfoFeignClient.getDriverLoginInfo(driverId).getData();
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        return driverInfoFeignClient.getDriverAuthInfo(driverId).getData();
    }

    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        return driverInfoFeignClient.updateDriverAuthInfo(updateDriverAuthInfoForm).getData();
    }

    @Override
    public Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        return driverInfoFeignClient.creatDriverFaceModel(driverFaceModelForm).getData();
    }
}
