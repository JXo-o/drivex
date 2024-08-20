package com.jxh.drivex.common.login.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ClassName: JwtProperties
 * Package: com.jxh.drivex.common.config.jwt
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/20 13:24
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private Duration expiration = Duration.ofHours(1);
    private String signKey = "psoEkv6hx2Sd72L00iIBSxlYZSteHyg6";

}
