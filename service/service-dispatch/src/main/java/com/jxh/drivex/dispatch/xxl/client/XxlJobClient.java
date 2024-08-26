package com.jxh.drivex.dispatch.xxl.client;

import com.alibaba.fastjson2.JSONObject;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.dispatch.xxl.config.XxlJobClientConfig;
import com.jxh.drivex.model.entity.dispatch.XxlJobInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * ClassName: XxlJobClient
 * Package: com.jxh.drivex.dispatch.xxl.client
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/26 18:47
 */
@Slf4j
@Component
public class XxlJobClient {

    private final XxlJobClientConfig xxlJobClientConfig;
    private final RestTemplate restTemplate;

    public XxlJobClient(
            XxlJobClientConfig xxlJobClientConfig,
            RestTemplate restTemplate
    ) {
        this.xxlJobClientConfig = xxlJobClientConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * 增加一个新的xxl-job。
     *
     * @param executorHandler 执行器的处理器
     * @param param           任务参数
     * @param corn            调度的Cron表达式
     * @param desc            任务描述
     * @return 创建的任务ID
     * @throws DrivexException 如果任务创建失败
     */
    @SneakyThrows
    public Long addJob(String executorHandler, String param, String corn, String desc){
        HttpEntity<XxlJobInfo> request = getXxlJobInfo(executorHandler, param, corn, desc);
        String url = xxlJobClientConfig.getAddUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(isSuccessful(response)) {
            log.info("增加xxl执行任务成功,返回信息:{}", Objects.requireNonNull(response.getBody()).toJSONString());
            return response.getBody().getLong("content");
        }
        log.info("调用xxl增加执行任务失败:{}", Objects.requireNonNull(response.getBody()).toJSONString());
        throw new DrivexException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     * 启动指定ID的xxl-job。
     *
     * @param jobId 任务ID
     * @return 启动成功返回true，否则抛出异常
     * @throws DrivexException 如果任务启动失败
     */
    public Boolean startJob(Long jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setId(jobId.intValue());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<XxlJobInfo> request = new HttpEntity<>(xxlJobInfo, headers);

        String url = xxlJobClientConfig.getStartJobUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(isSuccessful(response)) {
            log.info("启动xxl执行任务成功:{},返回信息:{}", jobId,
                    Objects.requireNonNull(response.getBody()).toJSONString());
            return true;
        }
        log.info("启动xxl执行任务失败:{},返回信息:{}", jobId, Objects.requireNonNull(response.getBody()).toJSONString());
        throw new DrivexException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     * 停止指定ID的xxl-job。
     *
     * @param jobId 任务ID
     * @return 停止成功返回true，否则抛出异常
     * @throws DrivexException 如果任务停止失败
     */
    public Boolean stopJob(Long jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setId(jobId.intValue());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<XxlJobInfo> request = new HttpEntity<>(xxlJobInfo, headers);

        String url = xxlJobClientConfig.getStopJobUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(isSuccessful(response)) {
            log.info("停止xxl执行任务成功:{},返回信息:{}", jobId,
                    Objects.requireNonNull(response.getBody()).toJSONString());
            return true;
        }
        log.info("停止xxl执行任务失败:{},返回信息:{}", jobId, Objects.requireNonNull(response.getBody()).toJSONString());
        throw new DrivexException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     * 删除指定ID的xxl-job。
     *
     * @param jobId 任务ID
     * @return 删除成功返回true，否则抛出异常
     * @throws DrivexException 如果任务删除失败
     */
    public Boolean removeJob(Long jobId) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setId(jobId.intValue());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<XxlJobInfo> request = new HttpEntity<>(xxlJobInfo, headers);

        String url = xxlJobClientConfig.getRemoveUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(isSuccessful(response)) {
            log.info("删除xxl执行任务成功:{},返回信息:{}", jobId,
                    Objects.requireNonNull(response.getBody()).toJSONString());
            return true;
        }
        log.info("删除xxl执行任务失败:{},返回信息:{}", jobId, Objects.requireNonNull(response.getBody()).toJSONString());
        throw new DrivexException(ResultCodeEnum.XXL_JOB_ERROR);
    }


    /**
     * 增加并启动xxl-job。
     *
     * @param executorHandler 执行器的处理器
     * @param param           任务参数
     * @param corn            调度的Cron表达式
     * @param desc            任务描述
     * @return 创建的任务ID
     * @throws DrivexException 如果任务增加或启动失败
     */
    public Long addAndStart(String executorHandler, String param, String corn, String desc) {
        HttpEntity<XxlJobInfo> request = getXxlJobInfo(executorHandler, param, corn, desc);

        String url = xxlJobClientConfig.getAddAndStartUrl();
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(url, request, JSONObject.class);
        if(isSuccessful(response)) {
            log.info("增加并开始执行xxl任务成功,返回信息:{}", Objects.requireNonNull(response.getBody()).toJSONString());
            return response.getBody().getLong("content");
        }
        log.info("增加并开始执行xxl任务失败:{}", Objects.requireNonNull(response.getBody()).toJSONString());
        throw new DrivexException(ResultCodeEnum.XXL_JOB_ERROR);
    }

    /**
     * 获取`XxlJobInfo`的`HttpEntity`。
     *
     * @param executorHandler 执行器的处理器
     * @param param           任务参数
     * @param corn            调度的Cron表达式
     * @param desc            任务描述
     * @return 包含任务信息的HttpEntity对象
     */
    private HttpEntity<XxlJobInfo> getXxlJobInfo(String executorHandler, String param, String corn, String desc) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();
        xxlJobInfo.setJobGroup(xxlJobClientConfig.getJobGroupId());
        xxlJobInfo.setJobDesc(desc);
        xxlJobInfo.setAuthor("JX");
        xxlJobInfo.setScheduleType("CRON");
        xxlJobInfo.setScheduleConf(corn);
        xxlJobInfo.setGlueType("BEAN");
        xxlJobInfo.setExecutorHandler(executorHandler);
        xxlJobInfo.setExecutorParam(param);
        xxlJobInfo.setExecutorRouteStrategy("FIRST");
        xxlJobInfo.setExecutorBlockStrategy("SERIAL_EXECUTION");
        xxlJobInfo.setMisfireStrategy("FIRE_ONCE_NOW");
        xxlJobInfo.setExecutorTimeout(0);
        xxlJobInfo.setExecutorFailRetryCount(0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(xxlJobInfo, headers);
    }

    /**
     * 检查响应是否成功。
     *
     * @param response 响应实体
     * @return 成功返回true，否则返回false
     */
    private Boolean isSuccessful(ResponseEntity<JSONObject> response) {
        return response.getStatusCode().value() == 200 &&
                Objects.requireNonNull(response.getBody()).getIntValue("code") == 200;
    }

}
