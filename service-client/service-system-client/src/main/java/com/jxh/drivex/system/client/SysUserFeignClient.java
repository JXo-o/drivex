package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysUser;
import com.jxh.drivex.model.query.system.SysUserQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "service-system")
public interface SysUserFeignClient {

    /**
     * 获取分页列表
     */
    @PostMapping("/sysUser/findPage/{page}/{limit}")
    Result<PageVo<SysUser>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysUserQuery sysUserQuery);

    /**
     * 获取用户
     */
    @GetMapping("/sysUser/getById/{id}")
    Result<SysUser> getById(@PathVariable("id") Long id);

    /**
     * 保存用户
     */
    @PostMapping("/sysUser/save")
    Result<Boolean> save(@RequestBody SysUser user);

    /**
     * 更新用户
     */
    @PutMapping("/sysUser/update")
    Result<Boolean> update(@RequestBody SysUser user);

    /**
     * 删除用户
     */
    @DeleteMapping("/sysUser/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id);

    /**
     * 更新状态
     */
    @GetMapping("/sysUser/updateStatus/{id}/{status}")
    Result<Boolean> updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status);

}

