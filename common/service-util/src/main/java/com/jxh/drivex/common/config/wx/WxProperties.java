package com.jxh.drivex.common.config.wx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: WxProperties
 * Package: com.jxh.drivex.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/20 9:57
 */
@Data
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxProperties {

    private String appId;
    private String secret;
    private String msgDataFormat = "JSON";

}
