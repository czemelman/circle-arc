package com.circle.arc.error;

/**
 * Standard error codes for the application
 */
public final class ErrorCode {

    private ErrorCode() {
        // Utility class
    }

    // Wallet errors
    public static final String WALLET_NOT_INITIALIZED = "WALLET_NOT_INITIALIZED";
    public static final String WALLET_CREATION_FAILED = "WALLET_CREATION_FAILED";
    public static final String INVALID_ADDRESS = "INVALID_ADDRESS";
    public static final String BALANCE_FETCH_FAILED = "BALANCE_FETCH_FAILED";

    // Transaction errors
    public static final String TRANSACTION_FAILED = "TRANSACTION_FAILED";
    public static final String TRANSACTION_STATUS_UNAVAILABLE = "TRANSACTION_STATUS_UNAVAILABLE";
    public static final String GAS_ESTIMATION_FAILED = "GAS_ESTIMATION_FAILED";
    public static final String NONCE_FETCH_FAILED = "NONCE_FETCH_FAILED";

    // Blockchain errors
    public static final String NETWORK_INFO_UNAVAILABLE = "NETWORK_INFO_UNAVAILABLE";
    public static final String BLOCK_NUMBER_UNAVAILABLE = "BLOCK_NUMBER_UNAVAILABLE";
    public static final String GAS_PRICE_UNAVAILABLE = "GAS_PRICE_UNAVAILABLE";
    public static final String CONNECTION_FAILED = "CONNECTION_FAILED";

    // General errors
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
}
