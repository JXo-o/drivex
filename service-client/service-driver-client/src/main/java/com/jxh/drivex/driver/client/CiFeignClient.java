package com.jxh.drivex.driver.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.vo.order.TextAuditingVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-driver")
public interface CiFeignClient {

    /**
     * 文本审核
     */
    @PostMapping("/ci/textAuditing")
    Result<TextAuditingVo> textAuditing(@RequestBody String content);
}