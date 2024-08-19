package com.jxh.drivex.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.model.entity.system.SysLoginLog;
import com.jxh.drivex.system.mapper.SysLoginLogMapper;
import com.jxh.drivex.system.service.SysLoginLogService;
import org.springframework.stereotype.Service;

@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog>
		implements SysLoginLogService {

}
