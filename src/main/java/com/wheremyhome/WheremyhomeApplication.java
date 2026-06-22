package com.wheremyhome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WheremyhomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WheremyhomeApplication.class, args);
    }
}
