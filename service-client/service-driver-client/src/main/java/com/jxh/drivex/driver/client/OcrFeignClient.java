package com.jxh.drivex.driver.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.vo.driver.DriverLicenseOcrVo;
import com.jxh.drivex.model.vo.driver.IdCardOcrVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "service-driver", contextId = "ocr")
public interface OcrFeignClient {

    /**
     * 身份证识别
     */
    @PostMapping(value = "/ocr/idCardOcr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<IdCardOcrVo> idCardOcr(@RequestPart("file") MultipartFile file);

    /**
     * 驾驶证识别
     */
    @PostMapping(value = "/ocr/driverLicenseOcr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<DriverLicenseOcrVo> driverLicenseOcr(@RequestPart("file") MultipartFile file);
}