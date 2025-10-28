package com.circle.arc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for transaction responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    /**
     * Transaction hash
     */
    private String transactionHash;

    /**
     * Sender address
     */
    private String from;

    /**
     * Recipient address
     */
    private String to;

    /**
     * Amount sent
     */
    private BigDecimal amount;

    /**
     * Currency symbol
     */
    private String currency;

    /**
     * Block number (null if pending)
     */
    private Long blockNumber;

    /**
     * Transaction status
     */
    private String status;

    /**
     * Gas used
     */
    private Long gasUsed;

    /**
     * Explorer URL for viewing transaction
     */
    private String explorerUrl;

    /**
     * Timestamp
     */
    private Long timestamp;
}
