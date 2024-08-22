package com.jxh.drivex.driver.service;

import com.jxh.drivex.model.vo.driver.CosUploadVo;
import org.springframework.web.multipart.MultipartFile;

public interface CosService {

    CosUploadVo upload(MultipartFile file, String path);
}
