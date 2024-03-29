package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

//修改
@SpringBootApplication
public class CommunityApplication {

    @PostConstruct
    public  void init(){
        // 解决netty启动冲突问题
        // redis -> netty; es -> netty 当redis启动后es会检查netty，发现已设置则会不启动，因此报错
        System.setProperty("es.set.netty.runtime.available.processors","false");
        // 从Netty4Utils.setAvailableProcessors()可发现
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
