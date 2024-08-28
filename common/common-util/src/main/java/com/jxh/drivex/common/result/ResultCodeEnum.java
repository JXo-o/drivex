package com.jxh.drivex.common.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),

    BAD_REQUEST(400, "请求错误"),
    WX_CODE_ERROR(400, "微信code错误"),
    VALIDATE_CODE_ERROR(400, "验证码错误"),
    PHONE_CODE_ERROR(400, "手机验证码错误"),
    XXL_JOB_ERROR(400, "xxl-job调用失败"),
    DROOLS_RULE_ERROR(400, "规则引擎错误"),
    START_LOCATION_DISTANCE_ERROR(400, "距离代驾起始点1公里以内才能确认"),
    END_LOCATION_DISTANCE_ERROR(400, "距离代驾终点2公里以内才能确认"),
    NODE_HAS_CHILDREN(400, "该节点下有子节点，不可以删除"),

    UNAUTHORIZED(401, "未登录"),
    TOKEN_EXPIRED(401, "token已过期"),
    TOKEN_INVALID(401, "token无效"),
    SIGNATURE_ERROR(401, "签名错误"),
    SIGNATURE_EXPIRED(401, "签名已过期"),
    AUTHENTICATION_REQUIRED(401, "认证通过后才可以开启代驾服务"),
    PASSWORD_ERROR(401, "密码错误"),

    FORBIDDEN(403, "没有权限"),
    ILLEGAL_REQUEST(403, "非法请求"),
    SERVICE_NOT_STARTED(403, "未开启代驾服务，不能更新位置信息"),
    FACE_RECOGNITION_FAILURE(403, "当日未进行人脸识别或识别失败"),
    COUPON_LIMIT_EXCEEDED(403, "超出领取数量"),
    ACCOUNT_DISABLED(403, "账号已停用"),

    ORDER_ID_NOT_FOUND(404, "订单id未找到"),
    NOT_FOUND(404, "资源未找到"),
    ACCOUNT_ERROR(404, "账号错误"),
    ACCOUNT_NOT_FOUND(404, "账号未找到"),

    CONFLICT(409, "冲突"),
    REPEAT_SUBMIT(409, "重复提交"),
    UPDATE_ERROR(409, "数据更新失败"),
    COUPON_OUT_OF_STOCK(409, "优惠券库存不足"),

    GONE(410, "资源已过期"),
    COUPON_EXPIRED(410, "优惠券已过期"),

    UNPROCESSABLE_ENTITY(422, "不可处理的实体"),
    DATA_ERROR(422, "数据错误"),
    ARGUMENT_VALIDATION_ERROR(422, "参数校验异常"),
    IMAGE_REVIEW_FAILURE(422, "图片审核不通过"),

    SERVICE_ERROR(500, "服务异常"),
    FEIGN_NULL_RESPONSE(500, "远程调用返回空"),
    ORDER_CREATION_FAILURE(500, "抢单失败"),
    PROFIT_SHARING_FAILURE(500, "分账调用失败"),

    FEIGN_FAILURE(502, "远程调用失败"),
    MAP_SERVICE_FAILURE(502, "地图服务调用失败");

    private final Integer code;

    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
