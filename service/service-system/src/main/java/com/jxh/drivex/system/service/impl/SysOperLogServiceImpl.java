package com.jxh.drivex.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.model.entity.system.SysOperLog;
import com.jxh.drivex.system.mapper.SysOperLogMapper;
import com.jxh.drivex.system.service.SysOperLogService;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>
		implements SysOperLogService {

}
