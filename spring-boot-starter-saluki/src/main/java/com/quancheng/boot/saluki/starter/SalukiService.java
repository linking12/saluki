package com.quancheng.boot.saluki.starter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

import com.quancheng.saluki.core.common.SalukiConstants;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface SalukiService {

    String interfaceName() default "";

    String group() default SalukiConstants.DEFAULT_GROUP;

    String version() default SalukiConstants.DEFAULT_VERSION;

}
