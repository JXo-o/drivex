package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysDept;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping(value="/sysDept")
public class SysDeptController {

    @Operation(summary = "根据id获取部门")
    @GetMapping("/getById/{id}")
    Result<SysDept> getById(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "保存部门")
    @PostMapping("/save")
    Result<Boolean> save(@RequestBody SysDept sysDept) {
        return Result.ok();
    }

    @Operation(summary = "更新部门")
    @PutMapping("/update")
    Result<Boolean> update(@RequestBody SysDept sysDept) {
        return Result.ok();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "获取全部部门节点")
    @GetMapping("/findNodes")
    Result<List<SysDept>> findNodes() {
        return Result.ok();
    }

    @Operation(summary = "获取用户部门节点")
    @GetMapping("/findUserNodes")
    Result<List<SysDept>> findUserNodes() {
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

