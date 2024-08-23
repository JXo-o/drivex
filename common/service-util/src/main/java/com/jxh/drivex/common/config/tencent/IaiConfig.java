package com.jxh.drivex.common.config.tencent;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: IaiConfig
 * Package: com.jxh.drivex.common.config.tencent
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/23 1:20
 */
@Configuration
@ConditionalOnClass(IaiClient.class)
@EnableConfigurationProperties(TencentCloudProperties.class)
public class IaiConfig {

    @Bean
    public IaiClient iaiClient(TencentCloudProperties tencentCloudProperties) {
        Credential cred = new Credential(
                tencentCloudProperties.getSecretId(),
                tencentCloudProperties.getSecretKey()
        );
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("iai.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new IaiClient(cred, tencentCloudProperties.getRegion(), clientProfile);
    }

}
