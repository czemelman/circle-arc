package com.circle.arc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for network information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkInfo {

    /**
     * Network name
     */
    private String networkName;

    /**
     * Chain ID
     */
    private Long chainId;

    /**
     * RPC URL
     */
    private String rpcUrl;

    /**
     * Current block number
     */
    private Long blockNumber;

    /**
     * Network status
     */
    private String status;

    /**
     * Client version
     */
    private String clientVersion;

    /**
     * Gas price (in Gwei)
     */
    private String gasPrice;

    /**
     * Native currency
     */
    private String nativeCurrency;

    /**
     * Explorer URL
     */
    private String explorerUrl;
}
