package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.driver.client.OcrFeignClient;
import com.jxh.drivex.driver.service.OcrService;
import com.jxh.drivex.model.vo.driver.DriverLicenseOcrVo;
import com.jxh.drivex.model.vo.driver.IdCardOcrVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    private final OcrFeignClient ocrFeignClient;

    public OcrServiceImpl(OcrFeignClient ocrFeignClient) {
        this.ocrFeignClient = ocrFeignClient;
    }

    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {
        return ocrFeignClient.idCardOcr(file).getData();
    }

    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        return ocrFeignClient.driverLicenseOcr(file).getData();
    }
}
