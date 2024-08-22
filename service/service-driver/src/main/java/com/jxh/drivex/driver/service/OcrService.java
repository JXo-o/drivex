package com.jxh.drivex.driver.service;

import com.jxh.drivex.model.vo.driver.DriverLicenseOcrVo;
import com.jxh.drivex.model.vo.driver.IdCardOcrVo;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

    IdCardOcrVo idCardOcr(MultipartFile file);

    DriverLicenseOcrVo driverLicenseOcr(MultipartFile file);
}
