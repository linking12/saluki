/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.quancheng.saluki.core.common.Constants;

/**
 * @author shimingliu 2016年12月16日 下午1:59:58
 * @version ThrallReference.java, v 0.0.1 2016年12月16日 下午1:59:58 shimingliu
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SalukiReference {

    String service() default "";

    String group() default "";

    String version() default "";

    int retries() default 0;

    String[] retryMethods() default {};

    boolean async() default true;

    int timeOut() default Constants.RPC_ASYNC_DEFAULT_TIMEOUT;

}
