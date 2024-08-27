package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.login.jwt.JwtUtil;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.dispatch.client.NewOrderFeignClient;
import com.jxh.drivex.driver.client.DriverInfoFeignClient;
import com.jxh.drivex.driver.service.DriverService;
import com.jxh.drivex.map.client.LocationFeignClient;
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
    private final LocationFeignClient locationFeignClient;
    private final NewOrderFeignClient newOrderFeignClient;

    public DriverServiceImpl(
            JwtUtil jwtUtil,
            DriverInfoFeignClient driverInfoFeignClient,
            LocationFeignClient locationFeignClient,
            NewOrderFeignClient newOrderFeignClient
    ) {
        this.jwtUtil = jwtUtil;
        this.driverInfoFeignClient = driverInfoFeignClient;
        this.locationFeignClient = locationFeignClient;
        this.newOrderFeignClient = newOrderFeignClient;
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

    @Override
    public Boolean isFaceRecognition(Long driverId) {
        return driverInfoFeignClient.isFaceRecognition(driverId).getData();
    }

    @Override
    public Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        return driverInfoFeignClient.verifyDriverFace(driverFaceModelForm).getData();
    }

    /**
     * <p>开始服务</p>
     * <ol>
     *     <li>判断认证状态</li>
     *     <li>判断当日是否人脸识别</li>
     *     <li>更新司机接单状态</li>
     *     <li>删除司机位置信息</li>
     *     <li>清空司机新订单队列</li>
     * </ol>
     * @param driverId 司机ID
     * @return 是否成功
     */
    @Override
    public Boolean startService(Long driverId) {
        DriverLoginVo driverLoginVo = driverInfoFeignClient.getDriverLoginInfo(driverId).getData();
        if(driverLoginVo.getAuthStatus() != 2) {
            throw new DrivexException(ResultCodeEnum.AUTHENTICATION_REQUIRED);
        }
        Boolean isFaceRecognition = driverInfoFeignClient.isFaceRecognition(driverId).getData();
        if(!isFaceRecognition) {
            throw new DrivexException(ResultCodeEnum.FACE_RECOGNITION_FAILURE);
        }
        driverInfoFeignClient.updateServiceStatus(driverId, 1);
        locationFeignClient.removeDriverLocation(driverId);
        newOrderFeignClient.clearNewOrderQueueData(driverId);
        return true;
    }

    /**
     * <p>停止服务</p>
     * <ol>
     *     <li>更新司机接单状态</li>
     *     <li>删除司机位置信息</li>
     *     <li>清空司机新订单队列</li>
     * </ol>
     * @param driverId 司机ID
     * @return 是否成功
     */
    @Override
    public Boolean stopService(Long driverId) {
        driverInfoFeignClient.updateServiceStatus(driverId, 0);
        locationFeignClient.removeDriverLocation(driverId);
        newOrderFeignClient.clearNewOrderQueueData(driverId);
        return true;
    }
}
