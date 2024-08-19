package com.jxh.drivex.common.execption;

import com.jxh.drivex.common.result.ResultCodeEnum;
import lombok.Data;

import java.io.Serial;

/**
 * 自定义全局异常类
 *
 */
@Data
public class DrivexException extends RuntimeException {


    @Serial
    private static final long serialVersionUID = 621913373297747933L;

    private Integer code;

    public DrivexException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public DrivexException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "DrivexException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
