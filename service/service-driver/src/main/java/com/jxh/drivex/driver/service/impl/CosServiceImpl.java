package com.jxh.drivex.driver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.jxh.drivex.common.config.tencent.TencentCloudProperties;
import com.jxh.drivex.driver.service.CosService;
import com.jxh.drivex.model.vo.driver.CosUploadVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class CosServiceImpl implements CosService {

    private final TencentCloudProperties tencentCloudProperties;
    private final COSClient cosClient;

    public CosServiceImpl(
            TencentCloudProperties tencentCloudProperties,
            COSClient cosClient
    ) {
        this.tencentCloudProperties = tencentCloudProperties;
        this.cosClient = cosClient;
    }

    @Override
    @SneakyThrows
    public CosUploadVo upload(MultipartFile file, String path) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentEncoding("UTF-8");
        meta.setContentType(file.getContentType());
        String fileType = FilenameUtils.getExtension(file.getOriginalFilename());
        String uploadPath = "/driver/" + path + "/" +
                UUID.randomUUID().toString().replaceAll("-", "") +
                "." + fileType;

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                tencentCloudProperties.getBucketPrivate(),
                uploadPath,
                file.getInputStream(),
                meta
        );

        putObjectRequest.setStorageClass(StorageClass.Standard);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        log.info(JSON.toJSONString(putObjectResult));
        // cosClient.shutdown();

        CosUploadVo cosUploadVo = new CosUploadVo();
        cosUploadVo.setUrl(uploadPath);
        cosUploadVo.setShowUrl(this.getImageUrl(uploadPath));
        return cosUploadVo;
    }

    @Override
    public String getImageUrl(String path) {
        URL url = cosClient.generatePresignedUrl(
                tencentCloudProperties.getBucketPrivate(),
                path,
                new Date(System.currentTimeMillis() + 15L * 60L * 1000L),
                HttpMethodName.GET
        );
        // cosClient.shutdown();
        return url.toString();
    }
}
