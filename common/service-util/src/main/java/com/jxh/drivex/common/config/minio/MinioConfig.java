package com.jxh.drivex.common.config.minio;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: MinioConfig
 * Package: com.jxh.drivex.common.config.minio
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/29 20:08
 */
@Configuration
@ConditionalOnClass(MinioClient.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

}
