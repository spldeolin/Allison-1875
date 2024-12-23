package com.spldeolin.allison1875.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表该DTO作为属性时包装在Collection中
 *
 * 适配Allison 1875 handler-transformer
 *
 * @author Deolin 2020-01-11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface L {

}
