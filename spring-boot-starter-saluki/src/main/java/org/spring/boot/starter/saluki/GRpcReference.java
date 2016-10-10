package org.spring.boot.starter.saluki;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface GRpcReference {

    String interfaceName() default "";

    String group() default "";

    String version() default "";

    boolean localProcess() default false;

    // blocking async future
    String callType() default "future";

}
