package com.jxh.drivex.common.config.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: MinioProperties
 * Package: com.jxh.drivex.common.config.minio
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/29 20:07
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

}