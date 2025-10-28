package com.circle.arc.controller;

import com.circle.arc.dto.WalletInfo;
import com.circle.arc.error.ErrorInfo;
import com.circle.arc.service.WalletService;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for wallet operations
 * Compliant with coding standards: handles Either return types, single return, no .get() on Optional
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Get current wallet information
     * Complies with Rule 3.1: Single return statement
     * Complies with Rule 4.1: No .get() on Optional/Either
     *
     * @return Wallet info including address and balance
     */
    @GetMapping("/info")
    public ResponseEntity<?> getWalletInfo() {
        return walletService.getWalletInfo()
            .fold(
                error -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error),
                walletInfo -> ResponseEntity.ok(walletInfo)
            );
    }

    /**
     * Get wallet address
     * Complies with Rule 3.1: Single return statement
     * Complies with Rule 4.1: Uses Optional.map instead of .get()
     *
     * @return Current wallet address
     */
    @GetMapping("/address")
    public ResponseEntity<?> getAddress() {
        return walletService.getCredentials()
            .map(credentials -> {
                final Map<String, String> response = new HashMap<>();
                response.put("address", credentials.getAddress());
                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> {
                final ErrorInfo error = ErrorInfo.of(
                    "WALLET_NOT_INITIALIZED",
                    "Wallet credentials not available"
                );
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            });
    }

    /**
     * Get balance for a specific address
     * Complies with Rule 3.1: Single return statement
     *
     * @param address Ethereum address to check
     * @return Balance in ARC
     */
    @GetMapping("/balance/{address}")
    public ResponseEntity<?> getBalance(@PathVariable String address) {
        return walletService.getBalance(address)
            .fold(
                error -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error),
                balance -> {
                    final Map<String, Object> response = new HashMap<>();
                    response.put("address", address);
                    response.put("balance", balance);
                    response.put("currency", "ARC");
                    return ResponseEntity.ok(response);
                }
            );
    }

    /**
     * Create a new wallet
     * Complies with Rule 3.1: Single return statement
     *
     * @return New wallet information
     */
    @PostMapping("/create")
    public ResponseEntity<?> createWallet() {
        return walletService.createNewWallet()
            .fold(
                error -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error),
                credentials -> {
                    final Map<String, String> response = new HashMap<>();
                    response.put("address", credentials.getAddress());
                    response.put("privateKey", "0x" + credentials.getEcKeyPair().getPrivateKey().toString(16));
                    response.put("warning", "SAVE YOUR PRIVATE KEY SECURELY! This is the only time it will be displayed.");
                    return ResponseEntity.ok(response);
                }
            );
    }
}
