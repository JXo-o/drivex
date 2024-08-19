package com.jxh.drivex.driver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.driver.mapper.DriverAccountMapper;
import com.jxh.drivex.driver.service.DriverAccountService;
import com.jxh.drivex.model.entity.driver.DriverAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DriverAccountServiceImpl extends ServiceImpl<DriverAccountMapper, DriverAccount>
        implements DriverAccountService {

}
