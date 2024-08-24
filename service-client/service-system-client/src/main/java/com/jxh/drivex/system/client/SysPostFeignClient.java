package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysPost;
import com.jxh.drivex.model.query.system.SysPostQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-system", contextId = "sysPost")
public interface SysPostFeignClient {

    /**
     * 分页查询岗位
     */
    @PostMapping("/sysPost/findPage/{page}/{limit}")
    Result<PageVo<SysPost>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysPostQuery sysPostQuery
    );

    /**
     * 根据ID查询岗位
     */
    @GetMapping("/sysPost/getById/{id}")
    Result<SysPost> getById(@PathVariable("id") Long id);

    /**
     * 查询所有岗位
     */
    @GetMapping("/sysPost/findAll")
    Result<List<SysPost>> findAll();

    /**
     * 保存岗位
     */
    @PostMapping("/sysPost/save")
    Result<Boolean> save(@RequestBody SysPost sysPost);

    /**
     * 更新岗位
     */
    @PutMapping("/sysPost/update")
    Result<Boolean> update(@RequestBody SysPost sysPost);

    /**
     * 删除岗位
     */
    @DeleteMapping("/sysPost/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id);

    /**
     * 更新岗位状态
     */
    @GetMapping("/sysPost/updateStatus/{id}/{status}")
    Result<Boolean> updateStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") Integer status
    );

}

