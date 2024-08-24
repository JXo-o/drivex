package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysPost;
import com.jxh.drivex.model.query.system.SysPostQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位管理")
@RestController
@RequestMapping(value="/sysPost")
public class SysPostController {

    @Operation(summary = "分页查询岗位")
    @PostMapping("/findPage/{page}/{limit}")
    Result<PageVo<SysPost>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysPostQuery sysPostQuery
    ) {
        return Result.ok();
    }

    @Operation(summary = "根据ID查询岗位")
    @GetMapping("/getById/{id}")
    Result<SysPost> getById(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "查询所有岗位")
    @GetMapping("/findAll")
    Result<List<SysPost>> findAll() {
        return Result.ok();
    }

    @Operation(summary = "保存岗位")
    @PostMapping("/save")
    Result<Boolean> save(@RequestBody SysPost sysPost) {
        return Result.ok();
    }

    @Operation(summary = "更新岗位")
    @PutMapping("/update")
    Result<Boolean> update(@RequestBody SysPost sysPost) {
        return Result.ok();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id) {
        return Result.ok();
    }

    @Operation(summary = "更新岗位状态")
    @GetMapping("/updateStatus/{id}/{status}")
    Result<Boolean> updateStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") Integer status
    ) {
        return Result.ok();
    }
}
