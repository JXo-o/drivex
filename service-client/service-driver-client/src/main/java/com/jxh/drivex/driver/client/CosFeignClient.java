package com.jxh.drivex.driver.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.vo.driver.CosUploadVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "service-driver", contextId = "cos")
public interface CosFeignClient {

    /**
     * 上传文件
     */
    @PostMapping(value = "/cos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<CosUploadVo> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("path") String path
    );

}