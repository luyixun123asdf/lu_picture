package com.lu.lupicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) // 表明该注解在运行时生效
public @interface AuthCheck {
    /**
     * 必须有某个角色
     *
     * @return
     */
    String mustRole() default "";
}
