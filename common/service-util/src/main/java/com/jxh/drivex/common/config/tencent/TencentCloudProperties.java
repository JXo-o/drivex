package com.jxh.drivex.common.config.tencent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName: TencentCloudProperties
 * Package: com.jxh.drivex.common.config.tencent
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/22 14:33
 */
@Data
@ConfigurationProperties(prefix = "tencent.cloud")
public class TencentCloudProperties {

    private String secretId;
    private String secretKey;
    private String region;
    private String bucketPrivate;
    private String personGroupId;

}
