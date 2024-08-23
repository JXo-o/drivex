package com.jxh.drivex.customer.service;

import com.jxh.drivex.model.form.customer.ExpectOrderForm;
import com.jxh.drivex.model.vo.customer.ExpectOrderVo;

public interface OrderService {

    ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);
}
