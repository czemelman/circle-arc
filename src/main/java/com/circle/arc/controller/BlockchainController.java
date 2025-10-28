package com.circle.arc.controller;

import com.circle.arc.dto.NetworkInfo;
import com.circle.arc.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for blockchain information
 */
@Slf4j
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    /**
     * Get network information
     *
     * @return Network details including chain ID, block number, etc.
     */
    @GetMapping("/network")
    public ResponseEntity<NetworkInfo> getNetworkInfo() {
        try {
            NetworkInfo networkInfo = blockchainService.getNetworkInfo();
            return ResponseEntity.ok(networkInfo);
        } catch (Exception e) {
            log.error("Failed to get network info: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get network information: " + e.getMessage());
        }
    }

    /**
     * Get current block number
     *
     * @return Current block number
     */
    @GetMapping("/block-number")
    public ResponseEntity<Map<String, Long>> getBlockNumber() {
        try {
            Long blockNumber = blockchainService.getCurrentBlockNumber();
            Map<String, Long> response = new HashMap<>();
            response.put("blockNumber", blockNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get block number: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get block number: " + e.getMessage());
        }
    }

    /**
     * Get current gas price
     *
     * @return Current gas price in Wei
     */
    @GetMapping("/gas-price")
    public ResponseEntity<Map<String, String>> getGasPrice() {
        try {
            BigInteger gasPrice = blockchainService.getCurrentGasPrice();
            Map<String, String> response = new HashMap<>();
            response.put("gasPriceWei", gasPrice.toString());
            response.put("gasPriceGwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)).toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get gas price: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get gas price: " + e.getMessage());
        }
    }

    /**
     * Check if connected to blockchain
     *
     * @return Connection status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean connected = blockchainService.isConnected();
        Map<String, Object> response = new HashMap<>();
        response.put("connected", connected);
        response.put("status", connected ? "CONNECTED" : "DISCONNECTED");
        return ResponseEntity.ok(response);
    }
}
