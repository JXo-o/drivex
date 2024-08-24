package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysOperLog;
import com.jxh.drivex.model.query.system.SysOperLogQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统操作日志管理")
@RestController
@RequestMapping(value="/sysOperLog")
public class SysOperLogController {

    @Operation(summary = "分页查询系统操作日志")
    @PostMapping("/findPage/{page}/{limit}")
    public Result<PageVo<SysOperLog>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysOperLogQuery sysOperLogQuery
    ) {
        return Result.ok();
    }

    @Operation(summary = "根据id查询系统操作日志")
    @GetMapping("/getById/{id}")
    Result<SysOperLog> getById(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "记录日志")
    @PostMapping("/saveSysLog")
    Result<Boolean> saveSysLog(@RequestBody SysOperLog sysOperLog) {
        return Result.ok();
    }
}
