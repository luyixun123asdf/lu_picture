package com.lu.lupicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@MapperScan("com.lu.lupicturebackend.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true) // 开启AOP的代理
public class LuPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuPictureBackendApplication.class, args);
    }

}
