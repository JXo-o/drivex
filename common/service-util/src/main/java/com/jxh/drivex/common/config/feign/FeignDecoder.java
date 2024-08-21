package com.jxh.drivex.common.config.feign;

import com.jxh.drivex.common.result.Result;
import com.jxh.drivex.common.result.ResultCodeEnum;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * ClassName: FeignDecoder
 * Package: com.jxh.drivex.common.config.feign
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/21 11:45
 */
public class FeignDecoder implements Decoder {

    private final SpringDecoder decoder;

    public FeignDecoder(SpringDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException {
        Object object = this.decoder.decode(response, type);
        if (object == null) {
            throw new DecodeException(
                    ResultCodeEnum.FEIGN_NULL_RESPONSE.getCode(),
                    ResultCodeEnum.FEIGN_NULL_RESPONSE.getMessage(),
                    response.request()
            );
        }
        if(object instanceof Result<?> result) {
            if (!result.isOk()) {
                throw new DecodeException(result.getCode(), result.getMessage(), response.request());
            }
            if (result.getData() == null) {
                throw new DecodeException(
                        ResultCodeEnum.FEIGN_NULL_RESPONSE.getCode(),
                        ResultCodeEnum.FEIGN_NULL_RESPONSE.getMessage(),
                        response.request()
                );
            }
            return result;
        }
        return object;
    }

}
