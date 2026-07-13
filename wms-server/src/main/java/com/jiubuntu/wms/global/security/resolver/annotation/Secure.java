package com.jiubuntu.wms.global.security.resolver.annotation;

import com.jiubuntu.wms.biz.user.domain.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secure {

    UserRole[] value() default {};

}
