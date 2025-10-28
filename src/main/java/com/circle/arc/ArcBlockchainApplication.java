package com.circle.arc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Circle Arc Blockchain Integration
 *
 * This application demonstrates how to connect to the Circle Arc blockchain testnet
 * and perform various operations including:
 * - Connecting to Arc testnet
 * - Creating and managing wallets
 * - Checking balances
 * - Sending transactions
 * - Monitoring transaction status
 */
@SpringBootApplication
public class ArcBlockchainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArcBlockchainApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("Circle Arc Blockchain Application Started!");
        System.out.println("===========================================");
        System.out.println("API Documentation: http://localhost:8080/swagger-ui.html");
        System.out.println("Health Check: http://localhost:8080/actuator/health");
        System.out.println("===========================================\n");
    }
}
