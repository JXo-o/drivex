package com.jxh.drivex.driver.controller;

import com.jxh.drivex.common.login.DrivexLogin;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.driver.service.CosService;
import com.jxh.drivex.model.vo.driver.CosUploadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "腾讯云cos上传接口管理")
@RestController
@RequestMapping(value="/cos")
public class CosController {

    private final CosService cosService;

    public CosController(CosService cosService) {
        this.cosService = cosService;
    }

    @DrivexLogin
    @Operation(summary = "上传")
    @PostMapping("/upload")
    public Result<CosUploadVo> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "path", defaultValue = "auth") String path
    ) {
        return Result.ok(cosService.upload(file, path));
    }

}

