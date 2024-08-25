package com.jxh.drivex.customer.service;

import com.jxh.drivex.model.form.customer.ExpectOrderForm;
import com.jxh.drivex.model.form.customer.SubmitOrderForm;
import com.jxh.drivex.model.vo.customer.ExpectOrderVo;

public interface OrderService {

    ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);

    Long submitOrder(SubmitOrderForm submitOrderForm);

    Integer getOrderStatus(Long orderId);
}
