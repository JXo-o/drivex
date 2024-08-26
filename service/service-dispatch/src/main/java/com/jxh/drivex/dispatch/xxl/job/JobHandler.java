package com.jxh.drivex.dispatch.xxl.job;

import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.jxh.drivex.dispatch.mapper.XxlJobLogMapper;
import com.jxh.drivex.dispatch.service.NewOrderService;
import com.jxh.drivex.model.entity.dispatch.XxlJobLog;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: JobHandler
 * Package: com.jxh.drivex.dispatch.xxl.job
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/26 19:56
 */
@Slf4j
@Component
public class JobHandler {

    private final XxlJobLogMapper xxlJobLogMapper;

    private final NewOrderService newOrderService;

    public JobHandler(XxlJobLogMapper xxlJobLogMapper, NewOrderService newOrderService) {
        this.xxlJobLogMapper = xxlJobLogMapper;
        this.newOrderService = newOrderService;
    }

    /**
     * 新订单调度任务的执行处理器。
     */
    @XxlJob("newOrderTaskHandler")
    public void newOrderTaskHandler() {
        log.info("新订单调度任务：{}", XxlJobHelper.getJobId());
        XxlJobLog xxlJobLog = new XxlJobLog();
        xxlJobLog.setJobId(XxlJobHelper.getJobId());
        long startTime = System.currentTimeMillis();
        try {
            newOrderService.executeTask(XxlJobHelper.getJobId());
            xxlJobLog.setStatus(1);
        } catch (Exception e) {
            xxlJobLog.setStatus(0);
            xxlJobLog.setError(ExceptionUtil.getAllExceptionMsg(e));
            log.error("定时任务执行失败,任务id为:{},异常信息为：{}", XxlJobHelper.getJobId(), e.getMessage(), e);
        } finally {
            int times = (int) (System.currentTimeMillis() - startTime);
            xxlJobLog.setTimes(times);
            xxlJobLogMapper.insert(xxlJobLog);
        }
    }

}
