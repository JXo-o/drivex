package com.jxh.drivex.common.config.tencent;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: OcrConfig
 * Package: com.jxh.drivex.common.config.tencent
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/22 21:09
 */
@EnableConfigurationProperties(TencentCloudProperties.class)
public class OcrConfig {

    @Bean
    public OcrClient ocrClient(TencentCloudProperties tencentCloudProperties) {
        Credential cred = new Credential(
                tencentCloudProperties.getSecretId(),
                tencentCloudProperties.getSecretKey()
        );
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ocr.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new OcrClient(cred, tencentCloudProperties.getRegion(), clientProfile);
    }

}
