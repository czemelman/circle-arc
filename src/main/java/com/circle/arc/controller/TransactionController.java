package com.circle.arc.controller;

import com.circle.arc.dto.TransactionRequest;
import com.circle.arc.dto.TransactionResponse;
import com.circle.arc.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for transaction operations
 */
@Slf4j
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Send a transaction to transfer ARC tokens
     *
     * @param request Transaction request containing recipient and amount
     * @return Transaction response with hash and status
     */
    @PostMapping("/send")
    public ResponseEntity<TransactionResponse> sendTransaction(
            @Valid @RequestBody TransactionRequest request) {
        try {
            log.info("Sending transaction: {} ARC to {}", request.getAmount(), request.getToAddress());
            TransactionResponse response = transactionService.sendTransaction(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send transaction: " + e.getMessage());
        }
    }

    /**
     * Get transaction status by hash
     *
     * @param transactionHash Transaction hash to look up
     * @return Transaction details and status
     */
    @GetMapping("/status/{transactionHash}")
    public ResponseEntity<TransactionResponse> getTransactionStatus(
            @PathVariable String transactionHash) {
        try {
            log.info("Getting status for transaction: {}", transactionHash);
            TransactionResponse response = transactionService.getTransactionStatus(transactionHash);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get transaction status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get transaction status: " + e.getMessage());
        }
    }

    /**
     * Estimate gas for a transaction
     *
     * @param from Sender address
     * @param to Recipient address
     * @param amount Amount to send
     * @return Estimated gas required
     */
    @GetMapping("/estimate-gas")
    public ResponseEntity<Map<String, Object>> estimateGas(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        try {
            BigInteger gasEstimate = transactionService.estimateGas(from, to, amount);
            Map<String, Object> response = new HashMap<>();
            response.put("estimatedGas", gasEstimate.toString());
            response.put("from", from);
            response.put("to", to);
            response.put("amount", amount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to estimate gas: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to estimate gas: " + e.getMessage());
        }
    }
}
