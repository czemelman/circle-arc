package com.circle.arc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for wallet management
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "arc.wallet")
public class WalletProperties {

    /**
     * Private key for the wallet (should be stored securely)
     * In production, use environment variables or a secure vault
     */
    private String privateKey;

    /**
     * Whether to automatically create a new wallet if none exists
     */
    private boolean enableAutoCreate = true;
}
