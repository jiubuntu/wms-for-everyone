package com.jiubuntu.wms.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("모두의 WMS API")
                        .description("모두의 WMS 서버 API 명세")
                        .version("v0.0.1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

}
