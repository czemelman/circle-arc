package com.circle.arc.controller;

import com.circle.arc.dto.TransactionRequest;
import com.circle.arc.dto.TransactionResponse;
import com.circle.arc.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for transaction operations
 * Compliant with coding standards: handles Either return types, single return
 */
@Slf4j
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Send a transaction to transfer ARC tokens
     * Complies with Rule 3.1: Single return statement
     *
     * @param request Transaction request containing recipient and amount
     * @return Transaction response with hash and status
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("Sending transaction: {} ARC to {}", request.getAmount(), request.getToAddress());

        return transactionService.sendTransaction(request)
            .fold(
                error -> {
                    log.error("Failed to send transaction: {}", error.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                },
                response -> {
                    log.info("Transaction sent successfully: {}", response.getTransactionHash());
                    return ResponseEntity.ok(response);
                }
            );
    }

    /**
     * Get transaction status by hash
     * Complies with Rule 3.1: Single return statement
     *
     * @param transactionHash Transaction hash to look up
     * @return Transaction details and status
     */
    @GetMapping("/status/{transactionHash}")
    public ResponseEntity<?> getTransactionStatus(@PathVariable String transactionHash) {
        log.info("Getting status for transaction: {}", transactionHash);

        return transactionService.getTransactionStatus(transactionHash)
            .fold(
                error -> {
                    log.error("Failed to get transaction status: {}", error.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                },
                response -> ResponseEntity.ok(response)
            );
    }

    /**
     * Estimate gas for a transaction
     * Complies with Rule 3.1: Single return statement
     *
     * @param from Sender address
     * @param to Recipient address
     * @param amount Amount to send
     * @return Estimated gas required
     */
    @GetMapping("/estimate-gas")
    public ResponseEntity<?> estimateGas(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        return transactionService.estimateGas(from, to, amount)
            .fold(
                error -> {
                    log.error("Failed to estimate gas: {}", error.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                },
                gasEstimate -> {
                    final Map<String, Object> response = new HashMap<>();
                    response.put("estimatedGas", gasEstimate.toString());
                    response.put("from", from);
                    response.put("to", to);
                    response.put("amount", amount);
                    return ResponseEntity.ok(response);
                }
            );
    }
}
