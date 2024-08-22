package com.jxh.drivex.driver.controller;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.vo.order.TextAuditingVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "腾讯云CI审核接口管理")
@RestController
@RequestMapping(value="/ci")
public class CiController {

    @Operation(summary = "文本审核")
    @PostMapping("/textAuditing")
    Result<TextAuditingVo> textAuditing(@RequestBody String content){
        return Result.ok(new TextAuditingVo());
    }

}
