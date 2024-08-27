package com.jxh.drivex.driver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxh.drivex.model.entity.driver.DriverInfo;
import com.jxh.drivex.model.entity.driver.DriverSet;
import com.jxh.drivex.model.form.driver.DriverFaceModelForm;
import com.jxh.drivex.model.form.driver.UpdateDriverAuthInfoForm;
import com.jxh.drivex.model.vo.driver.DriverAuthInfoVo;
import com.jxh.drivex.model.vo.driver.DriverLoginVo;

public interface DriverInfoService extends IService<DriverInfo> {

    Long login(String code);

    DriverLoginVo getDriverLoginInfo(Long driverId);

    DriverAuthInfoVo getDriverAuthInfo(Long driverId);

    Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm);

    Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm);

    DriverSet getDriverSet(Long driverId);

    Boolean isFaceRecognition(Long driverId);

    Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm);

    Boolean updateServiceStatus(Long driverId, Integer status);
}
