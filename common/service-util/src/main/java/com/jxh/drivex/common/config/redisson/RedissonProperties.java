package com.jxh.drivex.common.config.redisson;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: RedissonProperties
 * Package: com.jxh.drivex.common.config.redisson
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/19 14:45
 */

@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonProperties {

    private String host;

    private String password;

    private String port;

    private int timeout = 3000;

    private static String ADDRESS_PREFIX = "redis://";

    public String getAddress() {
        return ADDRESS_PREFIX + host + ":" + port;
    }

}
