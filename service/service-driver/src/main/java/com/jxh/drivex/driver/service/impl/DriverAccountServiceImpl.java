package com.jxh.drivex.driver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.driver.mapper.DriverAccountDetailMapper;
import com.jxh.drivex.driver.mapper.DriverAccountMapper;
import com.jxh.drivex.driver.service.DriverAccountService;
import com.jxh.drivex.model.entity.driver.DriverAccount;
import com.jxh.drivex.model.entity.driver.DriverAccountDetail;
import com.jxh.drivex.model.form.driver.TransferForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DriverAccountServiceImpl extends ServiceImpl<DriverAccountMapper, DriverAccount>
        implements DriverAccountService {

    private final DriverAccountMapper driverAccountMapper;
    private final DriverAccountDetailMapper driverAccountDetailMapper;

    public DriverAccountServiceImpl(
            DriverAccountMapper driverAccountMapper,
            DriverAccountDetailMapper driverAccountDetailMapper
    ) {
        this.driverAccountMapper = driverAccountMapper;
        this.driverAccountDetailMapper = driverAccountDetailMapper;
    }

    /**
     * 转账
     *
     * @param transferForm 转账表单
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean transfer(TransferForm transferForm) {
        long count = driverAccountDetailMapper.selectCount(
                new LambdaQueryWrapper<DriverAccountDetail>()
                        .eq(DriverAccountDetail::getTradeNo, transferForm.getTradeNo())
        );
        if(count > 0) return true;

        driverAccountMapper.add(transferForm.getDriverId(), transferForm.getAmount());
        DriverAccountDetail driverAccountDetail = new DriverAccountDetail();
        BeanUtils.copyProperties(transferForm, driverAccountDetail);
        driverAccountDetailMapper.insert(driverAccountDetail);
        return true;
    }
}
