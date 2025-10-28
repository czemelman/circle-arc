package com.circle.arc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for wallet information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletInfo {

    /**
     * Wallet address
     */
    private String address;

    /**
     * Balance in native currency (ARC)
     */
    private BigDecimal balance;

    /**
     * Balance in Wei (smallest unit)
     */
    private String balanceWei;

    /**
     * Currency symbol
     */
    private String currency;

    /**
     * Network name
     */
    private String network;

    /**
     * Chain ID
     */
    private Long chainId;
}
