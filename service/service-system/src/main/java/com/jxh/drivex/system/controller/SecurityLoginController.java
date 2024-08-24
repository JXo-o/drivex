package com.jxh.drivex.system.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.entity.system.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "security登录管理")
@RestController
@RequestMapping(value="/securityLogin")
public class SecurityLoginController {

    @Operation(summary = "根据用户名获取用户信息")
    @GetMapping("/getByUsername/{username}")
    Result<SysUser> getByUsername(@PathVariable("username") String username) {
        return Result.ok();
    }

    @Operation(summary = "获取用户按钮权限")
    @GetMapping("/findUserPermsList/{userId}")
    Result<List<String>> findUserPermsList(@PathVariable("userId") Long userId) {
        return Result.ok();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo/{userId}")
    Result<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId) {
        return Result.ok();
    }

}

