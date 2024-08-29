package com.jxh.drivex.driver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.jxh.drivex.common.config.tencent.TencentCloudProperties;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.driver.service.CiService;
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
    private final CiService ciService;

    public CosServiceImpl(
            TencentCloudProperties tencentCloudProperties,
            COSClient cosClient,
            CiService ciService
    ) {
        this.tencentCloudProperties = tencentCloudProperties;
        this.cosClient = cosClient;
        this.ciService = ciService;
    }

    /**
     * 上传文件至腾讯云 COS，并返回文件的存储路径及访问 URL。
     * <ol>
     *      <li>设置文件的元数据，包括内容长度、编码和类型。</li>
     *      <li>生成唯一的文件路径和名称，并创建 `PutObjectRequest` 请求对象。</li>
     *      <li>使用 COS 客户端执行文件上传，并记录上传结果的日志。</li>
     *      <li>创建 `CosUploadVo` 对象，设置文件的存储路径和访问 URL。</li>
     *      <li>使用腾讯云服务对图片进行审核。</li>
     *      <li>返回包含上传文件信息的 `CosUploadVo` 对象。</li>
     * </ol>
     *
     * @param file 要上传的文件
     * @param path 文件存储路径（不包含文件名）
     * @return 返回包含文件存储路径和访问 URL 的 `CosUploadVo` 对象
     */
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

        Boolean isAuditing = ciService.imageAuditing(uploadPath);
        if(!isAuditing) {
            cosClient.deleteObject(tencentCloudProperties.getBucketPrivate(), uploadPath);
            throw new DrivexException(ResultCodeEnum.IMAGE_AUDITING_ERROR);
        }

        CosUploadVo cosUploadVo = new CosUploadVo();
        cosUploadVo.setUrl(uploadPath);
        cosUploadVo.setShowUrl(this.getImageUrl(uploadPath));
        return cosUploadVo;
    }

    /**
     * 获取文件在 COS 中的临时访问 URL。
     * <ol>
     *      <li>生成一个有效期为 15 分钟的临时访问 URL。</li>
     *      <li>返回生成的 URL 字符串。</li>
     * </ol>
     *
     * @param path 文件在 COS 中的存储路径
     * @return 文件的临时访问 URL
     */
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
