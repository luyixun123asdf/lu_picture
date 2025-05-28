package com.lu.lupicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.lu.lupicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 开启AOP的代理
public class LuPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuPictureBackendApplication.class, args);
    }

}
