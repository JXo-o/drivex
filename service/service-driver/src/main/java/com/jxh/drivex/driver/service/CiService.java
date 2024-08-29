package com.jxh.drivex.driver.service;

import com.jxh.drivex.model.vo.order.TextAuditingVo;

public interface CiService {

    Boolean imageAuditing(String path);

    TextAuditingVo textAuditing(String content);
}
