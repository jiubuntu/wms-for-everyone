package com.jiubuntu.wms.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.global.idempotency.IdempotencyFilter;
import com.jiubuntu.wms.global.infrastructure.IdempotencyKeyStore;
import com.jiubuntu.wms.global.security.resolver.AuthPrincipalArgumentResolver;
import com.jiubuntu.wms.global.security.interceptor.SecureInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecureInterceptor());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthPrincipalArgumentResolver());
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(RequestMappingHandlerMapping handlerMapping,
                                                                         IdempotencyKeyStore idempotencyKeyStore,
                                                                         ObjectMapper objectMapper) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>(
                new IdempotencyFilter(handlerMapping, idempotencyKeyStore, objectMapper));
        registration.addUrlPatterns("/api/*");
        return registration;
    }

}
