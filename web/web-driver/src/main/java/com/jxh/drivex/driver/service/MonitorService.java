package com.jxh.drivex.driver.service;

import com.jxh.drivex.model.form.order.OrderMonitorForm;
import org.springframework.web.multipart.MultipartFile;

public interface MonitorService {

    Boolean upload(MultipartFile file, OrderMonitorForm orderMonitorForm);
}
