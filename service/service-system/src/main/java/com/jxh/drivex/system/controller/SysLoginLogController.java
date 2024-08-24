package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysLoginLog;
import com.jxh.drivex.model.query.system.SysLoginLogQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统登录日志管理")
@RestController
@RequestMapping(value="/sysLoginLog")
public class SysLoginLogController {

    @Operation(summary = "分页查询登录日志")
    @PostMapping("/findPage/{page}/{limit}")
    Result<PageVo<SysLoginLog>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysLoginLogQuery sysLoginLogQuery
    ) {
        return Result.ok();
    }

    @Operation(summary = "根据ID获取登录日志")
    @GetMapping("/getById/{id}")
    Result<SysLoginLog> getById(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "记录登录日志")
    @PostMapping("/recordLoginLog")
    Result<Boolean> recordLoginLog(@RequestBody SysLoginLog sysLoginLog) {
        return Result.ok();
    }

}
