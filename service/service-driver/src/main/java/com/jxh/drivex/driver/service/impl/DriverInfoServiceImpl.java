package com.jxh.drivex.driver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.driver.mapper.DriverInfoMapper;
import com.jxh.drivex.driver.service.DriverInfoService;
import com.jxh.drivex.model.entity.driver.DriverInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo>
        implements DriverInfoService {

}