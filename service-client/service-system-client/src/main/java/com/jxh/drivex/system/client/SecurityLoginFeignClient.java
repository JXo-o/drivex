package com.jxh.drivex.system.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(value = "service-system")
public interface SecurityLoginFeignClient {

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/securityLogin/getByUsername/{username}")
    Result<SysUser> getByUsername(@PathVariable("username") String username);

    /**
     * 获取用户按钮权限
     */
    @GetMapping("/securityLogin/findUserPermsList/{userId}")
    Result<List<String>> findUserPermsList(@PathVariable("userId") Long userId);

    /**
     * 获取用户信息
     */
    @GetMapping("/securityLogin/getUserInfo/{userId}")
    Result<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId);

}