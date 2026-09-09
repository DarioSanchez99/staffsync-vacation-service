package com.staffsync.vacation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VacationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacationServiceApplication.class, args);
    }
}
