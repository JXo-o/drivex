package com.jxh.drivex.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jxh.drivex.model.entity.system.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SysPostMapper extends BaseMapper<SysPost> {

}
