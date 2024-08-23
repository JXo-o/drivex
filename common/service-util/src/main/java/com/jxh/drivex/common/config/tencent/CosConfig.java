package com.jxh.drivex.common.config.tencent;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: CosConfig
 * Package: com.jxh.drivex.common.config.tencent
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/22 14:35
 */
@Configuration
@ConditionalOnClass(COSClient.class)
@EnableConfigurationProperties(TencentCloudProperties.class)
public class CosConfig {

    @Bean
    public COSClient cosClient(TencentCloudProperties tencentCloudProperties) {
        COSCredentials cred = new BasicCOSCredentials(
                tencentCloudProperties.getSecretId(),
                tencentCloudProperties.getSecretKey()
        );
        ClientConfig clientConfig = new ClientConfig(new Region(tencentCloudProperties.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(cred, clientConfig);
    }

}
