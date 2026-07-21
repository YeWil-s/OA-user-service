package com.oa.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    String[] value() default {};

    /** If true, user must have ALL specified permissions. If false, ANY is sufficient. */
    boolean matchAll() default false;
}
