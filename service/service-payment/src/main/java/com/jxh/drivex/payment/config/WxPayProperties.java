package com.jxh.drivex.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: WxPayProperties
 * Package: com.jxh.drivex.payment.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/31 22:42
 */
@Data
@ConfigurationProperties(prefix="wx.v3pay")
public class WxPayProperties {

    private String appid;

    /** 商户号 */
    public String merchantId;

    /** 商户API私钥路径 */
    public String privateKeyPath;

    /** 商户证书序列号 */
    public String merchantSerialNumber;

    /** 商户API V3密钥 */
    public String apiV3key;

    /** 回调地址 */
    private String notifyUrl;

}
