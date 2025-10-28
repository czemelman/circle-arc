package com.circle.arc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for transaction requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    /**
     * Recipient address (must be a valid Ethereum-style address)
     */
    @NotBlank(message = "Recipient address is required")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Invalid Ethereum address format")
    private String toAddress;

    /**
     * Amount to send in ARC (will be converted to Wei)
     */
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    /**
     * Optional transaction data (in hex format)
     */
    private String data;

    /**
     * Optional gas price (if not specified, default will be used)
     */
    private Long gasPrice;

    /**
     * Optional gas limit (if not specified, default will be used)
     */
    private Long gasLimit;
}
