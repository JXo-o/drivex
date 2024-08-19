package com.jxh.drivex.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxh.drivex.model.entity.system.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

}
