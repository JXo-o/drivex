package com.jxh.drivex.driver.service.impl;

import com.jxh.drivex.common.config.tencent.TencentCloudProperties;
import com.jxh.drivex.driver.service.CiService;
import com.jxh.drivex.model.vo.order.TextAuditingVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.auditing.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CiServiceImpl implements CiService {

    private final TencentCloudProperties tencentCloudProperties;
    private final COSClient cosClient;

    public CiServiceImpl(
            TencentCloudProperties tencentCloudProperties,
            COSClient cosClient
    ) {
        this.tencentCloudProperties = tencentCloudProperties;
        this.cosClient = cosClient;
    }

    /**
     * 使用腾讯云服务对图片进行审核
     * @param path 图片路径
     * @return 是否合规
     */
    @Override
    public Boolean imageAuditing(String path) {
        ImageAuditingRequest request = new ImageAuditingRequest();
        request.setBucketName(tencentCloudProperties.getBucketPrivate());
        request.setObjectKey(path);
        ImageAuditingResponse response = cosClient.imageAuditing(request);
        cosClient.shutdown();
        return response.getPornInfo().getHitFlag().equals("0")
                && response.getAdsInfo().getHitFlag().equals("0")
                && response.getTerroristInfo().getHitFlag().equals("0")
                && response.getPoliticsInfo().getHitFlag().equals("0");
    }

    /**
     * 使用腾讯云服务对文本进行审核
     * @param content 文本内容
     * @return 审核结果
     */
    @Override
    public TextAuditingVo textAuditing(String content) {
        if(!StringUtils.hasText(content)) {
            TextAuditingVo textAuditingVo = new TextAuditingVo();
            textAuditingVo.setResult("0");
            return textAuditingVo;
        }
        TextAuditingRequest request = new TextAuditingRequest();
        request.setBucketName(tencentCloudProperties.getBucketPrivate());

        byte[] encoder = Base64.encodeBase64(content.getBytes());
        String contentBase64 = new String(encoder);
        request.getInput().setContent(contentBase64);
        request.getConf().setDetectType("all");

        TextAuditingResponse response = cosClient.createAuditingTextJobs(request);
        AuditingJobsDetail detail = response.getJobsDetail();
        TextAuditingVo textAuditingVo = new TextAuditingVo();
        if ("Success".equals(detail.getState())) {
            String result = detail.getResult();
            StringBuilder keywords = new StringBuilder();
            List<SectionInfo> sectionInfoList = detail.getSectionList();
            for (SectionInfo info : sectionInfoList) {
                String pornInfoKeyword = info.getPornInfo().getKeywords();
                String illegalInfoKeyword = info.getIllegalInfo().getKeywords();
                String abuseInfoKeyword = info.getAbuseInfo().getKeywords();
                if (!pornInfoKeyword.isEmpty()) {
                    keywords.append(pornInfoKeyword).append(",");
                }
                if (!illegalInfoKeyword.isEmpty()) {
                    keywords.append(illegalInfoKeyword).append(",");
                }
                if (!abuseInfoKeyword.isEmpty()) {
                    keywords.append(abuseInfoKeyword).append(",");
                }
            }
            textAuditingVo.setResult(result);
            textAuditingVo.setKeywords(keywords.toString());
        }
        return textAuditingVo;
    }
}
