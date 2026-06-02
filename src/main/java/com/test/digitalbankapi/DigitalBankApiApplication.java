package com.test.digitalbankapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DigitalBankApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalBankApiApplication.class, args);
    }

}
