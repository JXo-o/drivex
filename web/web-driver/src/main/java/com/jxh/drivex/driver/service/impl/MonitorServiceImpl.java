package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.driver.client.CiFeignClient;
import com.jxh.drivex.driver.service.FileService;
import com.jxh.drivex.driver.service.MonitorService;
import com.jxh.drivex.model.entity.order.OrderMonitor;
import com.jxh.drivex.model.entity.order.OrderMonitorRecord;
import com.jxh.drivex.model.form.order.OrderMonitorForm;
import com.jxh.drivex.model.vo.order.TextAuditingVo;
import com.jxh.drivex.order.client.OrderMonitorFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {

    private final FileService fileService;
    private final OrderMonitorFeignClient orderMonitorFeignClient;
    private final CiFeignClient ciFeignClient;

    public MonitorServiceImpl(
            FileService fileService,
            OrderMonitorFeignClient orderMonitorFeignClient,
            CiFeignClient ciFeignClient
    ) {
        this.fileService = fileService;
        this.orderMonitorFeignClient = orderMonitorFeignClient;
        this.ciFeignClient = ciFeignClient;
    }

    /**
     * 审核及更新订单监控记录信息
     * @param file 文件
     * @param orderMonitorForm 订单监控表单
     * @return 是否上传成功
     */
    @Override
    public Boolean upload(MultipartFile file, OrderMonitorForm orderMonitorForm) {
        String url = fileService.upload(file);
        log.info("upload: {}", url);
        OrderMonitorRecord orderMonitorRecord = new OrderMonitorRecord();
        orderMonitorRecord.setOrderId(orderMonitorForm.getOrderId());
        orderMonitorRecord.setFileUrl(url);
        orderMonitorRecord.setContent(orderMonitorForm.getContent());

        TextAuditingVo textAuditingVo = ciFeignClient.textAuditing(orderMonitorForm.getContent()).getData();
        orderMonitorRecord.setResult(textAuditingVo.getResult());
        orderMonitorRecord.setKeywords(textAuditingVo.getKeywords());
        orderMonitorFeignClient.saveMonitorRecord(orderMonitorRecord);

        OrderMonitor orderMonitor = orderMonitorFeignClient.getOrderMonitor(orderMonitorForm.getOrderId()).getData();
        int fileNum = orderMonitor.getFileNum() + 1;
        orderMonitor.setFileNum(fileNum);
        // 审核结果: 0（审核正常），1 （判定为违规敏感文件），2（疑似敏感，建议人工复核）。
        if("2".equals(orderMonitorRecord.getResult())) {
            orderMonitor.setAuditNum(orderMonitor.getAuditNum() + 1);
        }
        orderMonitorFeignClient.updateOrderMonitor(orderMonitor);
        return true;
    }
}
