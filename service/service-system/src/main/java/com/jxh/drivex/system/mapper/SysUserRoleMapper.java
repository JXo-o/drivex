package com.jxh.drivex.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxh.drivex.model.entity.system.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}

