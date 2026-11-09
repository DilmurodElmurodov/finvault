package com.finvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FinVault - Premium Finance Intelligence Platform
 * Main Spring Boot Application Entry Point
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class FinVaultApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinVaultApplication.class, args);
    }
}
