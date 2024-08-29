package com.jxh.drivex.driver.controller;

import com.jxh.drivex.common.login.DrivexLogin;
import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.driver.service.MonitorService;
import com.jxh.drivex.model.form.order.OrderMonitorForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "监控接口管理")
@RestController
@RequestMapping(value="/monitor")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @DrivexLogin
    @Operation(summary = "上传录音")
    @PostMapping("/upload")
    public Result<Boolean> upload(@RequestParam("file") MultipartFile file,
                                  OrderMonitorForm orderMonitorForm) {
        return Result.ok(monitorService.upload(file, orderMonitorForm));
    }

}

