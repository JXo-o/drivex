package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysDept;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-system", contextId = "sysDept")
public interface SysDeptFeignClient {

    /**
     * 根据id获取部门
     */
    @GetMapping("/sysDept/getById/{id}")
    Result<SysDept> getById(@PathVariable("id") Long id);

    /**
     * 保存部门
     */
    @PostMapping("/sysDept/save")
    Result<Boolean> save(@RequestBody SysDept sysDept);

    /**
     * 更新部门
     */
    @PutMapping("/sysDept/update")
    Result<Boolean> update(@RequestBody SysDept sysDept);

    /**
     * 删除部门
     */
    @DeleteMapping("/sysDept/remove/{id}")
    Result<Boolean> remove(@PathVariable("id") Long id);

    /**
     * 获取全部部门节点
     */
    @GetMapping("/sysDept/findNodes")
    Result<List<SysDept>> findNodes();

    /**
     * 获取用户部门节点
     */
    @GetMapping("/sysDept/findUserNodes")
    Result<List<SysDept>> findUserNodes();

    /**
     * 更新状态
     */
    @GetMapping("/sysDept/updateStatus/{id}/{status}")
    Result<Boolean> updateStatus(
            @PathVariable("id") Long id,
            @PathVariable("status") Integer status
    );

}

