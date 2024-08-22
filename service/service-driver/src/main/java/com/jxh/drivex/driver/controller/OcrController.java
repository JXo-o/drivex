package com.jxh.drivex.driver.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.driver.service.OcrService;
import com.jxh.drivex.model.vo.driver.DriverLicenseOcrVo;
import com.jxh.drivex.model.vo.driver.IdCardOcrVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "腾讯云识别接口管理")
@RestController
@RequestMapping(value="/ocr")
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Operation(summary = "身份证识别")
    @PostMapping(value = "/idCardOcr")
    Result<IdCardOcrVo> idCardOcr(@RequestPart("file") MultipartFile file) {
        return Result.ok(ocrService.idCardOcr(file));
    }

    @Operation(summary = "驾驶证识别")
    @PostMapping(value = "/driverLicenseOcr")
    Result<DriverLicenseOcrVo> driverLicenseOcr(@RequestPart("file") MultipartFile file) {
        return Result.ok(ocrService.driverLicenseOcr(file));
    }

}

