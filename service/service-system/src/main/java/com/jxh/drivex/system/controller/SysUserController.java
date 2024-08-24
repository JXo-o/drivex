package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysUser;
import com.jxh.drivex.model.query.system.SysUserQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/sysUser")
@CrossOrigin
public class SysUserController {

    @Operation(summary = "获取分页列表")
    @PostMapping("/findPage/{page}/{limit}")
    Result<PageVo<SysUser>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysUserQuery sysUserQuery
    ) {
        return Result.ok();
    }

    @Operation(summary = "获取用户")
    @GetMapping("/getById/{id}")
    Result<SysUser> getById(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "保存用户")
    @PostMapping("/save")
    Result<Boolean> save(@RequestBody SysUser user) {
        return Result.ok();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update")
    Result<Boolean> update(@RequestBody SysUser user) {
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "更新状态")
    @GetMapping("/updateStatus/{id}/{status}")
    Result<Boolean> updateStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") Integer status
    ) {
        return Result.ok();
    }
}
