package com.jxh.drivex.common.config.knife4j;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("代驾API接口文档")
                        .version("1.0")
                        .description("代驾API接口文档")
                        .contact(new Contact()
                                .name("JX")
                                .email("23120357@bjtu.edu.cn")
                        )
                );
    }

    @Bean
    public GroupedOpenApi webApi() {
        return GroupedOpenApi.builder()
                .group("web-api")
                .pathsToMatch("/**")
                .build();
    }

}
