package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysOperLog;
import com.jxh.drivex.model.query.system.SysOperLogQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-system", contextId = "sysOperLog")
public interface SysOperLogFeignClient {

    /**
     * 分页查询系统操作日志
     */
    @PostMapping("/sysOperLog/findPage/{page}/{limit}")
    Result<PageVo<SysOperLog>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysOperLogQuery sysOperLogQuery
    );

    /**
     * 根据id查询系统操作日志
     */
    @GetMapping("/sysOperLog/getById/{id}")
    Result<SysOperLog> getById(@PathVariable("id") Long id);

    /**
     * 记录日志
     */
    @PostMapping("/sysOperLog/saveSysLog")
    Result<Boolean> saveSysLog(@RequestBody SysOperLog sysOperLog);
}