package com.jxh.drivex.payment.config;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: WxPayConfig
 * Package: com.jxh.drivex.payment.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/31 22:44
 */
@Configuration
@EnableConfigurationProperties(WxPayProperties.class)
public class WxPayConfig {

    private final WxPayProperties wxPayProperties;

    public WxPayConfig(WxPayProperties wxPayProperties) {
        this.wxPayProperties = wxPayProperties;
    }

    @Bean
    public RSAAutoCertificateConfig getConfig(){
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayProperties.getMerchantId())
                .privateKeyFromPath(wxPayProperties.getPrivateKeyPath())
                .merchantSerialNumber(wxPayProperties.getMerchantSerialNumber())
                .apiV3Key(wxPayProperties.getApiV3key())
                .build();
    }
}
