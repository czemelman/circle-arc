package com.circle.arc.controller;

import com.circle.arc.dto.WalletInfo;
import com.circle.arc.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for wallet operations
 */
@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Get current wallet information
     *
     * @return Wallet info including address and balance
     */
    @GetMapping("/info")
    public ResponseEntity<WalletInfo> getWalletInfo() {
        try {
            WalletInfo walletInfo = walletService.getWalletInfo();
            return ResponseEntity.ok(walletInfo);
        } catch (Exception e) {
            log.error("Failed to get wallet info: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get wallet information: " + e.getMessage());
        }
    }

    /**
     * Get wallet address
     *
     * @return Current wallet address
     */
    @GetMapping("/address")
    public ResponseEntity<Map<String, String>> getAddress() {
        try {
            String address = walletService.getCredentials().getAddress();
            Map<String, String> response = new HashMap<>();
            response.put("address", address);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get address: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get wallet address: " + e.getMessage());
        }
    }

    /**
     * Get balance for a specific address
     *
     * @param address Ethereum address to check
     * @return Balance in ARC
     */
    @GetMapping("/balance/{address}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String address) {
        try {
            BigDecimal balance = walletService.getBalance(address);
            Map<String, Object> response = new HashMap<>();
            response.put("address", address);
            response.put("balance", balance);
            response.put("currency", "ARC");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get balance for {}: {}", address, e.getMessage(), e);
            throw new RuntimeException("Failed to get balance: " + e.getMessage());
        }
    }

    /**
     * Create a new wallet
     *
     * @return New wallet information
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createWallet() {
        try {
            var credentials = walletService.createNewWallet();
            Map<String, String> response = new HashMap<>();
            response.put("address", credentials.getAddress());
            response.put("privateKey", "0x" + credentials.getEcKeyPair().getPrivateKey().toString(16));
            response.put("warning", "SAVE YOUR PRIVATE KEY SECURELY! This is the only time it will be displayed.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create wallet: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create wallet: " + e.getMessage());
        }
    }
}
