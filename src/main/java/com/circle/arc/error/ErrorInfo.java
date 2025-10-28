package com.circle.arc.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error information container
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {

    /**
     * Error code for programmatic handling
     */
    private String code;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Additional context or details
     */
    private String details;

    /**
     * Create error with code and message
     */
    public static ErrorInfo of(String code, String message) {
        return ErrorInfo.builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Create error with code, message, and details
     */
    public static ErrorInfo of(String code, String message, String details) {
        return ErrorInfo.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}
