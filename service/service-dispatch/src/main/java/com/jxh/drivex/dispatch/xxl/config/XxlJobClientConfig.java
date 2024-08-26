package com.jxh.drivex.dispatch.xxl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: XxlJobClientConfig
 * Package: com.jxh.drivex.dispatch.xxl.config
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/26 18:45
 */
@Data
@Component
@ConfigurationProperties(prefix = "xxl.job.client")
public class XxlJobClientConfig {

    private Integer jobGroupId;
    private String addUrl;
    private String removeUrl;
    private String startJobUrl;
    private String stopJobUrl;
    private String addAndStartUrl;

}