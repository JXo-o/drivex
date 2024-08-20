package com.jxh.drivex.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: WxConfig
 * Package: com.jxh.drivex.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/20 9:57
 */
@Configuration
@EnableConfigurationProperties(WxProperties.class)
public class WxConfig {

    @Bean
    public WxMaService wxMaService(WxProperties wxProperties) {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(wxProperties.getAppId());
        config.setSecret(wxProperties.getSecret());
        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }

}
