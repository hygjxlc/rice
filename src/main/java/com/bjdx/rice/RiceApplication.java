package com.bjdx.rice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "com.bjdx.rice")
@MapperScan("com.bjdx.rice.*.mapper")
public class RiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiceApplication.class, args);
    }

}
