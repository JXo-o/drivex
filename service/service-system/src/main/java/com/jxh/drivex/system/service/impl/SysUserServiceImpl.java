package com.jxh.drivex.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.model.entity.system.SysUser;
import com.jxh.drivex.system.mapper.SysUserMapper;
import com.jxh.drivex.system.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
		implements SysUserService {

}
