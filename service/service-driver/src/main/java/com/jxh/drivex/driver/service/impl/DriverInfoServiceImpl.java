package com.jxh.drivex.driver.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jxh.drivex.common.config.tencent.TencentCloudProperties;
import com.jxh.drivex.common.constant.SystemConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.driver.mapper.DriverAccountMapper;
import com.jxh.drivex.driver.mapper.DriverInfoMapper;
import com.jxh.drivex.driver.mapper.DriverLoginLogMapper;
import com.jxh.drivex.driver.mapper.DriverSetMapper;
import com.jxh.drivex.driver.service.CosService;
import com.jxh.drivex.driver.service.DriverInfoService;
import com.jxh.drivex.model.entity.driver.DriverAccount;
import com.jxh.drivex.model.entity.driver.DriverInfo;
import com.jxh.drivex.model.entity.driver.DriverLoginLog;
import com.jxh.drivex.model.entity.driver.DriverSet;
import com.jxh.drivex.model.form.driver.DriverFaceModelForm;
import com.jxh.drivex.model.form.driver.UpdateDriverAuthInfoForm;
import com.jxh.drivex.model.vo.driver.DriverAuthInfoVo;
import com.jxh.drivex.model.vo.driver.DriverLoginVo;
import com.tencentcloudapi.iai.v20200303.IaiClient;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonRequest;
import com.tencentcloudapi.iai.v20200303.models.CreatePersonResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo>
        implements DriverInfoService {

    private final WxMaService wxMaService;
    private final DriverSetMapper driverSetMapper;
    private final DriverAccountMapper driverAccountMapper;
    private final DriverLoginLogMapper driverLoginLogMapper;
    private final CosService cosService;
    private final IaiClient iaiClient;
    private final TencentCloudProperties tencentCloudProperties;

    public DriverInfoServiceImpl(
            WxMaService wxMaService,
            DriverSetMapper driverSetMapper,
            DriverAccountMapper driverAccountMapper,
            DriverLoginLogMapper driverLoginLogMapper,
            CosService cosService,
            IaiClient iaiClient,
            TencentCloudProperties tencentCloudProperties
    ) {
        this.wxMaService = wxMaService;
        this.driverSetMapper = driverSetMapper;
        this.driverAccountMapper = driverAccountMapper;
        this.driverLoginLogMapper = driverLoginLogMapper;
        this.cosService = cosService;
        this.iaiClient = iaiClient;
        this.tencentCloudProperties = tencentCloudProperties;
    }

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

        DriverInfo driverInfo = this.lambdaQuery()
                .eq(DriverInfo::getWxOpenId, openIdOptional.get())
                .one();

        DriverInfo driverInfoToUse = Optional.ofNullable(driverInfo).orElseGet(() -> {
            DriverInfo newDriverInfo = new DriverInfo();
            newDriverInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            newDriverInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            newDriverInfo.setWxOpenId(openIdOptional.get());
            this.save(newDriverInfo);

            DriverSet driverSet = new DriverSet();
            driverSet.setDriverId(newDriverInfo.getId());
            driverSet.setOrderDistance(new BigDecimal(0));
            driverSet.setAcceptDistance(new BigDecimal(SystemConstant.ACCEPT_DISTANCE));
            driverSet.setIsAutoAccept(0);
            driverSetMapper.insert(driverSet);

            DriverAccount driverAccount = new DriverAccount();
            driverAccount.setDriverId(newDriverInfo.getId());
            driverAccountMapper.insert(driverAccount);
            return newDriverInfo;
        });

        DriverLoginLog driverLoginLog = new DriverLoginLog();
        driverLoginLog.setDriverId(driverInfoToUse.getId());
        driverLoginLog.setMsg("小程序登录");
        driverLoginLogMapper.insert(driverLoginLog);
        return driverInfoToUse.getId();
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        DriverInfo driverInfo = this.getById(driverId);
        DriverLoginVo driverLoginVo = new DriverLoginVo();
        BeanUtils.copyProperties(driverInfo, driverLoginVo);
        driverLoginVo.setIsArchiveFace(Strings.isEmpty(driverInfo.getFaceModelId()));
        return driverLoginVo;
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        DriverInfo driverInfo = this.getById(driverId);
        DriverAuthInfoVo driverAuthInfoVo = new DriverAuthInfoVo();
        BeanUtils.copyProperties(driverInfo, driverAuthInfoVo);
        driverAuthInfoVo.setIdcardBackShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardBackUrl()));
        driverAuthInfoVo.setIdcardFrontShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardFrontUrl()));
        driverAuthInfoVo.setIdcardHandShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardHandUrl()));
        driverAuthInfoVo.setDriverLicenseFrontShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseFrontUrl()));
        driverAuthInfoVo.setDriverLicenseBackShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseBackUrl()));
        driverAuthInfoVo.setDriverLicenseHandShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseHandUrl()));
        return driverAuthInfoVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setId(updateDriverAuthInfoForm.getDriverId());
        BeanUtils.copyProperties(updateDriverAuthInfoForm, driverInfo);
        return this.updateById(driverInfo);
    }

    @Override
    @SneakyThrows
    public Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        DriverInfo driverInfo = this.getById(driverFaceModelForm.getDriverId());
        CreatePersonRequest req = new CreatePersonRequest();
        req.setGroupId(tencentCloudProperties.getPersonGroupId());
        req.setPersonId(String.valueOf(driverInfo.getId()));
        req.setGender(Long.parseLong(driverInfo.getGender()));
        req.setQualityControl(4L);
        req.setUniquePersonControl(4L);
        req.setPersonName(driverInfo.getName());
        req.setImage(driverFaceModelForm.getImageBase64());
        CreatePersonResponse resp = iaiClient.CreatePerson(req);
        log.info(JSON.toJSONString(resp));
        if (!Strings.isEmpty(driverInfo.getFaceModelId())) {
            driverInfo.setFaceModelId(resp.getFaceId());
            this.updateById(driverInfo);
        }
        return true;
    }
}