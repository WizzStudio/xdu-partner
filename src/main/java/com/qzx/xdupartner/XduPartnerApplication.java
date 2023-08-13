package com.qzx.xdupartner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.qzx.xdupartner.mapper")
@SpringBootApplication
@EnableScheduling
public class XduPartnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XduPartnerApplication.class, args);
    }

}
