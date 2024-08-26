package com.jxh.drivex.map.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.map.service.MapService;
import com.jxh.drivex.model.form.map.CalculateDrivingLineForm;
import com.jxh.drivex.model.vo.map.DrivingLineVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class MapServiceImpl implements MapService {

    private final String key;
    private final RestTemplate restTemplate;

    public MapServiceImpl(
            @Value("${tencent.map.key}") String key,
            RestTemplate restTemplate
    ) {
        this.key = key;
        this.restTemplate = restTemplate;
    }

    /**
     * 根据起点和终点的经纬度，调用腾讯地图API计算驾驶路线，并返回驾驶路线的详细信息。
     * <ol>
     *      <li>构造请求 URL，包括起点和终点坐标以及 API 密钥。</li>
     *      <li>使用 RestTemplate 发送 GET 请求获取地图服务返回的 JSON 数据。</li>
     *      <li>检查请求结果是否为空或状态码是否为 0，以确认请求是否成功。</li>
     *      <li>从 JSON 数据中提取路线信息，包括距离、时长和折线数据。</li>
     *      <li>将距离转换为公里并保留两位小数，将其他数据封装到 `DrivingLineVo` 对象中。</li>
     *      <li>返回封装好的 `DrivingLineVo` 对象。</li>
     * </ol>
     *
     * @param calculateDrivingLineForm 包含起点和终点坐标的计算请求表单对象
     * @return 返回计算得到的 `DrivingLineVo` 对象，包含距离、时长等
     * @throws DrivexException 如果调用地图服务失败或返回结果状态不为0，则抛出自定义异常
     */
    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        String url = "https://apis.map.qq.com/ws/direction/v1/driving/?from={from}&to={to}&key={key}";
        Map<String, String> map = new HashMap<>();
        map.put("from", calculateDrivingLineForm.getStartPointLatitude() + "," +
                calculateDrivingLineForm.getStartPointLongitude());
        map.put("to", calculateDrivingLineForm.getEndPointLatitude() + "," +
                calculateDrivingLineForm.getEndPointLongitude());
        map.put("key", key);

        JSONObject result = restTemplate.getForObject(url, JSONObject.class, map);
        if(result == null || result.getIntValue("status") != 0) {
            throw new DrivexException(ResultCodeEnum.MAP_SERVICE_FAILURE);
        }

        JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
        DrivingLineVo drivingLineVo = new DrivingLineVo();
        drivingLineVo.setDistance(
                route.getBigDecimal("distance")
                        .divideToIntegralValue(new BigDecimal(1000))
                        .setScale(2, RoundingMode.HALF_UP)
        );
        drivingLineVo.setDuration(route.getBigDecimal("duration"));
        drivingLineVo.setPolyline(route.getJSONArray("polyline"));
        return drivingLineVo;
    }
}
