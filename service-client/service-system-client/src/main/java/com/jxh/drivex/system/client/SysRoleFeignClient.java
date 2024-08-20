package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysRole;
import com.jxh.drivex.model.query.system.SysRoleQuery;
import com.jxh.drivex.model.vo.base.PageVo;
import com.jxh.drivex.model.vo.system.AssginRoleVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "service-system", contextId = "sysRole")
public interface SysRoleFeignClient {


    /**
     * 获取全部角色列表
     */
    @GetMapping("/sysRole/findAll")
    Result<List<SysRole>> findAll();

    /**
     * 获取分页列表
     */
    @PostMapping("/sysRole/findPage/{page}/{limit}")
    Result<PageVo<SysRole>> findPage(
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit,
            @RequestBody SysRoleQuery roleQuery);

    /**
     * 获取角色信息
     */
    @GetMapping("/sysRole/getById/{id}")
    Result<SysRole> getById(@PathVariable("id") Long id);

    /**
     * 新增角色
     */
    @PostMapping("/sysRole/save")
    Result<Boolean> save(@RequestBody @Validated SysRole role);

    /**
     * 修改角色
     */
    @PutMapping("/sysRole/update")
    Result<Boolean> update(@RequestBody SysRole role);

    /**
     * 删除角色
     */
    @DeleteMapping("/sysRole/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id);

    /**
     * 根据id列表删除
     */
    @DeleteMapping("/sysRole/batchRemove")
    Result<Boolean> batchRemove(@RequestBody List<Long> idList);

    /**
     * 根据用户获取角色数据
     */
    @GetMapping("/sysRole/toAssign/{userId}")
    Result<Map<String, Object>> toAssign(@PathVariable("userId") Long userId);

    /**
     * 根据用户分配角色
     */
    @PostMapping("/sysRole/doAssign")
    Result<Boolean> doAssign(@RequestBody AssginRoleVo assginRoleVo);

}

