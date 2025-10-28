package com.circle.arc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Arc Blockchain connection
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "arc.blockchain")
public class ArcBlockchainProperties {

    /**
     * Network name (e.g., "Arc Testnet")
     */
    private String networkName;

    /**
     * Chain ID for Arc Testnet (1244)
     */
    private Long chainId;

    /**
     * RPC endpoint URL
     */
    private String rpcUrl;

    /**
     * Block explorer URL
     */
    private String explorerUrl;

    /**
     * Native currency symbol
     */
    private String nativeCurrency;

    /**
     * Default gas price in Wei
     */
    private Long gasPrice;

    /**
     * Default gas limit
     */
    private Long gasLimit;
}
