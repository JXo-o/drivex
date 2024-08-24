package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysMenu;
import com.jxh.drivex.model.vo.system.AssginMenuVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/sysMenu")
public class SysMenuController {

    @Operation(summary = "获取菜单")
    @GetMapping("/findNodes")
    Result<List<SysMenu>> findNodes() {
        return Result.ok();
    }

    @Operation(summary = "保存菜单")
    @PostMapping("/save")
    Result<Boolean> save(@RequestBody SysMenu sysMenu) {
        return Result.ok();
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/update")
    Result<Boolean> update(@RequestBody SysMenu permission) {
        return Result.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "根据角色获取菜单")
    @GetMapping("/toAssign/{roleId}")
    Result<List<SysMenu>> toAssign(@PathVariable("roleId") Long roleId) {
        return Result.ok();
    }

    @Operation(summary = "给角色分配权限")
    @PostMapping("/doAssign")
    Result<Boolean> doAssign(@RequestBody AssginMenuVo assginMenuVo) {
        return Result.ok();
    }
}
