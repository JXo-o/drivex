package com.jxh.drivex.dispatch.service;

import com.jxh.drivex.model.vo.dispatch.NewOrderTaskVo;
import com.jxh.drivex.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface NewOrderService {

    Long addAndStartTask(NewOrderTaskVo newOrderTaskVo);

    void executeTask(long jobId);

    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);

    Boolean clearNewOrderQueueData(Long driverId);
}
