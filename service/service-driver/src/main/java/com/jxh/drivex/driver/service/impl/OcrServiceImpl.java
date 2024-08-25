package com.jxh.drivex.driver.service.impl;

import com.alibaba.nacos.common.codec.Base64;
import com.jxh.drivex.common.config.tencent.OcrConfig;
import com.jxh.drivex.driver.service.CosService;
import com.jxh.drivex.driver.service.OcrService;
import com.jxh.drivex.model.vo.driver.CosUploadVo;
import com.jxh.drivex.model.vo.driver.DriverLicenseOcrVo;
import com.jxh.drivex.model.vo.driver.IdCardOcrVo;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    private final OcrClient ocrClient;
    private final CosService cosService;

    public OcrServiceImpl(
            OcrClient ocrClient,
            CosService cosService
    ) {
        this.ocrClient = ocrClient;
        this.cosService = cosService;
    }

    /**
     * 处理身份证OCR识别请求。
     * <ol>
     *      <li>将上传的身份证图片转换为Base64编码。</li>
     *      <li>构建身份证OCR识别请求并调用OCR服务。</li>
     *      <li>根据OCR识别结果，封装身份证信息到 `IdCardOcrVo` 对象中。</li>
     *      <li>将身份证图片上传至COS，并设置相关URL。</li>
     *      <li>返回包含识别结果和图片URL的 `IdCardOcrVo` 对象。</li>
     * </ol>
     *
     * @param file 包含身份证图像的 `MultipartFile`
     * @return 识别出的身份证信息封装在 `IdCardOcrVo` 对象中
     */
    @Override
    @SneakyThrows
    public IdCardOcrVo idCardOcr(MultipartFile file) {
        byte[] encoder = Base64.encodeBase64(file.getBytes());
        String idCardBase64 = new String(encoder);
        IDCardOCRRequest req = new IDCardOCRRequest();
        req.setImageBase64(idCardBase64);
        IDCardOCRResponse resp = ocrClient.IDCardOCR(req);
        log.info(IDCardOCRResponse.toJsonString(resp));

        IdCardOcrVo idCardOcrVo = new IdCardOcrVo();
        if (!Strings.isEmpty(resp.getName())) {
            idCardOcrVo.setName(resp.getName());
            idCardOcrVo.setGender("男".equals(resp.getSex()) ? "1" : "2");
            idCardOcrVo.setBirthday(DateTimeFormat.forPattern("yyyy/MM/dd").parseDateTime(resp.getBirth()).toDate());
            idCardOcrVo.setIdcardNo(resp.getIdNum());
            idCardOcrVo.setIdcardAddress(resp.getAddress());

            CosUploadVo cosUploadVo = cosService.upload(file, "idCard");
            idCardOcrVo.setIdcardFrontUrl(cosUploadVo.getUrl());
            idCardOcrVo.setIdcardFrontShowUrl(cosUploadVo.getShowUrl());
        } else {
            String idcardExpireString = resp.getValidDate().split("-")[1];
            idCardOcrVo.setIdcardExpire(DateTimeFormat.forPattern("yyyy.MM.dd").parseDateTime(idcardExpireString).toDate());

            CosUploadVo cosUploadVo = cosService.upload(file, "idCard");
            idCardOcrVo.setIdcardBackUrl(cosUploadVo.getUrl());
            idCardOcrVo.setIdcardBackShowUrl(cosUploadVo.getShowUrl());
        }
        return idCardOcrVo;
    }

    /**
     * 处理驾驶证OCR识别请求。
     * <ol>
     *      <li>将上传的驾驶证图片转换为Base64编码。</li>
     *      <li>构建驾驶证OCR识别请求并调用OCR服务。</li>
     *      <li>根据OCR识别结果，封装驾驶证信息到 `DriverLicenseOcrVo` 对象中。</li>
     *      <li>将驾驶证图片上传至COS，并设置相关URL。</li>
     *      <li>返回包含识别结果和图片URL的 `DriverLicenseOcrVo` 对象。</li>
     * </ol>
     *
     * @param file 包含驾驶证图像的 `MultipartFile`
     * @return 识别出的驾驶证信息封装在 `DriverLicenseOcrVo` 对象中
     */
    @Override
    @SneakyThrows
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        byte[] encoder = Base64.encodeBase64(file.getBytes());
        String driverLicenseBase64 = new String(encoder);
        DriverLicenseOCRRequest req = new DriverLicenseOCRRequest();
        req.setImageBase64(driverLicenseBase64);
        DriverLicenseOCRResponse resp = ocrClient.DriverLicenseOCR(req);
        log.info(VehicleLicenseOCRResponse.toJsonString(resp));

        DriverLicenseOcrVo driverLicenseOcrVo = new DriverLicenseOcrVo();
        if (!Strings.isEmpty(resp.getName())) {
            driverLicenseOcrVo.setName(resp.getName());
            driverLicenseOcrVo.setDriverLicenseClazz(resp.getClass_());
            driverLicenseOcrVo.setDriverLicenseNo(resp.getCardCode());
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            driverLicenseOcrVo.setDriverLicenseIssueDate(formatter.parseDateTime(resp.getDateOfFirstIssue()).toDate());
            driverLicenseOcrVo.setDriverLicenseExpire(formatter.parseDateTime(resp.getEndDate()).toDate());

            CosUploadVo cosUploadVo = cosService.upload(file, "driverLicense");
            driverLicenseOcrVo.setDriverLicenseFrontUrl(cosUploadVo.getUrl());
            driverLicenseOcrVo.setDriverLicenseFrontShowUrl(cosUploadVo.getShowUrl());
        } else {
            CosUploadVo cosUploadVo =  cosService.upload(file, "driverLicense");
            driverLicenseOcrVo.setDriverLicenseBackUrl(cosUploadVo.getUrl());
            driverLicenseOcrVo.setDriverLicenseBackShowUrl(cosUploadVo.getShowUrl());
        }

        return driverLicenseOcrVo;
    }
}
