package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysMenu;
import com.jxh.drivex.model.vo.system.AssginMenuVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-system", contextId = "sysMenu")
public interface SysMenuFeignClient {

    /**
     * 获取菜单
     */
    @GetMapping("/sysMenu/findNodes")
    Result<List<SysMenu>> findNodes();

    @PostMapping("/sysMenu/save")
    Result<Boolean> save(@RequestBody SysMenu sysMenu);

    @PutMapping("/sysMenu/update")
    Result<Boolean> update(@RequestBody SysMenu permission);

    @DeleteMapping("/sysMenu/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id);

    /**
     * 根据角色获取菜单
     */
    @GetMapping("/sysMenu/toAssign/{roleId}")
    Result<List<SysMenu>> toAssign(@PathVariable("roleId") Long roleId);

    /**
     * 给角色分配权限
     */
    @PostMapping("/sysMenu/doAssign")
    Result<Boolean> doAssign(@RequestBody AssginMenuVo assginMenuVo);
}

