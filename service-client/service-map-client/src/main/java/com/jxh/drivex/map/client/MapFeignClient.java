package com.jxh.drivex.map.client;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.vo.map.DrivingLineVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-map", contextId = "map")
public interface MapFeignClient {

    /**
     * 计算驾驶线路
     */
    @PostMapping("/map/calculateDrivingLine")
    Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm);
}