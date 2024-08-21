package com.jxh.drivex.common.config.feign;

import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: FeignConfig
 * Package: com.jxh.drivex.common.config.feign
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/21 11:45
 */
@Configuration
public class FeignConfig {

    @Bean
    public Decoder decoder(
            ObjectFactory<HttpMessageConverters> msgConverters,
            ObjectProvider<HttpMessageConverterCustomizer> customizers
    ) {
        return new OptionalDecoder(
                new ResponseEntityDecoder(
                        new FeignDecoder(
                                new SpringDecoder(msgConverters, customizers)
                        )
                )
        );
    }

}
