package com.quancheng.boot.saluki.starter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.quancheng.saluki.core.common.SalukiConstants;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface SalukiReference {

    String service();

    String group();

    String version();

    boolean localProcess() default false;

    int requestTime() default SalukiConstants.DEFAULT_TIMEOUT;

    int callType() default SalukiConstants.RPCTYPE_ASYNC;

}
