package com.oa.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    String[] value() default {};

    /** If true, user must have ALL specified roles. If false, ANY role is sufficient. */
    boolean matchAll() default false;
}
