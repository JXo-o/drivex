package com.jxh.drivex.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.model.entity.system.SysMenu;
import com.jxh.drivex.system.mapper.SysMenuMapper;
import com.jxh.drivex.system.service.SysMenuService;
import org.springframework.stereotype.Service;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

}
