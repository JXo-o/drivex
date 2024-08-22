package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.driver.client.CosFeignClient;
import com.jxh.drivex.driver.service.CosService;
import com.jxh.drivex.model.vo.driver.CosUploadVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class CosServiceImpl implements CosService {

    private final CosFeignClient cosFeignClient;

    public CosServiceImpl(CosFeignClient cosFeignClient) {
        this.cosFeignClient = cosFeignClient;
    }

    @Override
    public CosUploadVo upload(MultipartFile file, String path) {
        return cosFeignClient.upload(file, path).getData();
    }
}
