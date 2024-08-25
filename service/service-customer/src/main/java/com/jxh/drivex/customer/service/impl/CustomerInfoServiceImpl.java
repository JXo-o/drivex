package com.jxh.drivex.customer.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.customer.mapper.CustomerInfoMapper;
import com.jxh.drivex.customer.mapper.CustomerLoginLogMapper;
import com.jxh.drivex.customer.service.CustomerInfoService;
import com.jxh.drivex.model.entity.customer.CustomerInfo;
import com.jxh.drivex.model.entity.customer.CustomerLoginLog;
import com.jxh.drivex.model.form.customer.UpdateWxPhoneForm;
import com.jxh.drivex.model.vo.customer.CustomerLoginVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo>
        implements CustomerInfoService {

    private final WxMaService wxMaService;
    private final CustomerLoginLogMapper customerLoginLogMapper;

    public CustomerInfoServiceImpl(
            WxMaService wxMaService,
            CustomerLoginLogMapper customerLoginLogMapper
    ) {
        this.wxMaService = wxMaService;
        this.customerLoginLogMapper = customerLoginLogMapper;
    }

    /**
     * 处理用户微信小程序登录逻辑。
     * <ol>
     *      <li>根据提供的code获取用户的openid。</li>
     *      <li>检查用户是否存在。如果是首次登录，则创建新用户记录。</li>
     *      <li>记录用户登录日志。</li>
     *      <li>返回用户ID。</li>
     * </ol>
     *
     * @param code 微信小程序登录时返回的code
     * @return 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long login(String code) {
        Optional<String> openIdOptional;
        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openIdOptional = Optional.of(sessionInfo.getOpenid());
        } catch (WxErrorException e) {
            log.error("login error", e);
            throw new DrivexException(ResultCodeEnum.WX_CODE_ERROR);
        }

        CustomerInfo customerInfo = this.lambdaQuery()
                .eq(CustomerInfo::getWxOpenId, openIdOptional.get())
                .one();

        CustomerInfo customerInfoToUse = Optional.ofNullable(customerInfo).orElseGet(() -> {
            CustomerInfo newCustomerInfo = new CustomerInfo();
            newCustomerInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            newCustomerInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            newCustomerInfo.setWxOpenId(openIdOptional.get());
            this.save(newCustomerInfo);
            return newCustomerInfo;
        });

        CustomerLoginLog customerLoginLog = new CustomerLoginLog();
        customerLoginLog.setCustomerId(customerInfoToUse.getId());
        customerLoginLog.setMsg("小程序登录");
        customerLoginLogMapper.insert(customerLoginLog);
        return customerInfoToUse.getId();
    }

    /**
     * 获取用户登录信息，包括是否绑定手机号等。
     * <ol>
     *      <li>根据用户ID获取用户信息。</li>
     *      <li>将用户信息封装到 `CustomerLoginVo` 对象中。</li>
     *      <li>检查用户是否绑定手机号，并设置相应标记。</li>
     * </ol>
     *
     * @param customerId 用户ID
     * @return 用户登录信息的VO对象
     */
    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        CustomerInfo customerInfo = this.getById(customerId);
        CustomerLoginVo customerInfoVo = new CustomerLoginVo();
        BeanUtils.copyProperties(customerInfo, customerInfoVo);
        customerInfoVo.setIsBindPhone(Strings.isEmpty(customerInfo.getPhone()));
        return customerInfoVo;
    }

    /**
     * 更新用户的微信绑定手机号。
     * <ol>
     *      <li>通过提供的code获取微信手机号信息。</li>
     *      <li>将手机号信息更新到数据库中。</li>
     * </ol>
     *
     * @param updateWxPhoneForm 包含用户ID和微信code的表单
     * @return 更新操作是否成功
     */
    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(updateWxPhoneForm.getCode());
        String phoneNumber = phoneInfo.getPhoneNumber();
        log.info("phoneInfo:{}", JSON.toJSONString(phoneInfo));
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setId(updateWxPhoneForm.getCustomerId());
        customerInfo.setPhone(phoneNumber);
        return this.updateById(customerInfo);
    }
}
