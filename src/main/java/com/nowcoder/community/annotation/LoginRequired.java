package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//作用在什么位置
@Retention(RetentionPolicy.RUNTIME)//运行时有效
public @interface LoginRequired {
}
