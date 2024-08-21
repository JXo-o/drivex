package com.jxh.drivex.model.vo.map;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrivingLineVo {

	@Schema(description = "方案总距离，单位：千米")
	private BigDecimal distance;

	@Schema(description = "方案估算时间（结合路况），单位：分钟")
	private BigDecimal duration;

	@Schema(description = "方案路线坐标点串")
	private JSONArray polyline;

}