package com.quancheng.saluki.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModelMapping {

    String mapping() default "";
}
