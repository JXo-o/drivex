package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysRole;
import com.jxh.drivex.model.query.system.SysRoleQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.system.AssginRoleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "角色管理")
@RestController
@RequestMapping("/sysRole")
public class SysRoleController {

    @Operation(summary = "获取全部角色列表")
    @GetMapping("/findAll")
    Result<List<SysRole>> findAll() {
        return Result.ok();
    }

    @Operation(summary = "获取分页列表")
    @PostMapping("/findPage/{page}/{limit}")
    Result<PageVo<SysRole>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysRoleQuery roleQuery
    ) {
        return Result.ok();
    }

    @Operation(summary = "获取角色信息")
    @GetMapping("/getById/{id}")
    Result<SysRole> getById(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "新增角色")
    @PostMapping("/save")
    Result<Boolean> save(@RequestBody @Validated SysRole role) {
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping("/update")
    Result<Boolean> update(@RequestBody SysRole role) {
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    Result<Boolean> batchRemove(@RequestBody List<Long> idList) {
        return Result.ok();
    }

    @Operation(summary = "根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    Result<Map<String, Object>> toAssign(@PathVariable("userId") Long userId) {
        return Result.ok();
    }

    @Operation(summary = "根据用户分配角色")
    @PostMapping("/doAssign")
    Result<Boolean> doAssign(@RequestBody AssginRoleVo assginRoleVo) {
        return Result.ok();
    }
}
