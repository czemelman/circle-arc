package com.circle.arc.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.Executors;

/**
 * Web3j configuration for connecting to Arc blockchain
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class Web3jConfig {

    private final ArcBlockchainProperties blockchainProperties;

    /**
     * Creates and configures Web3j instance for Arc testnet
     */
    @Bean
    public Web3j web3j() {
        log.info("Initializing Web3j connection to Arc Testnet...");
        log.info("RPC URL: {}", blockchainProperties.getRpcUrl());
        log.info("Chain ID: {}", blockchainProperties.getChainId());

        HttpService httpService = new HttpService(blockchainProperties.getRpcUrl());

        // Create Web3j instance with custom thread pool
        Web3j web3j = Web3j.build(httpService, 1000, Executors.newScheduledThreadPool(5));

        // Test connection
        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("Successfully connected to Arc Testnet. Client version: {}", clientVersion);
        } catch (Exception e) {
            log.error("Failed to connect to Arc Testnet: {}", e.getMessage());
            log.warn("Application will continue, but blockchain operations may fail");
        }

        return web3j;
    }
}
