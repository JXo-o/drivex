package com.jxh.drivex.driver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jxh.drivex.model.entity.driver.DriverAccount;
import com.jxh.drivex.model.form.driver.TransferForm;

public interface DriverAccountService extends IService<DriverAccount> {

    Boolean transfer(TransferForm transferForm);
}
