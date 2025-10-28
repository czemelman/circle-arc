package com.circle.arc.controller;

import com.circle.arc.dto.NetworkInfo;
import com.circle.arc.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for blockchain information
 * Compliant with coding standards: handles Either return types, single return
 */
@Slf4j
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    /**
     * Get network information
     * Complies with Rule 3.1: Single return statement
     *
     * @return Network details including chain ID, block number, etc.
     */
    @GetMapping("/network")
    public ResponseEntity<?> getNetworkInfo() {
        return blockchainService.getNetworkInfo()
            .fold(
                error -> {
                    log.error("Failed to get network info: {}", error.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                },
                networkInfo -> ResponseEntity.ok(networkInfo)
            );
    }

    /**
     * Get current block number
     * Complies with Rule 3.1: Single return statement
     *
     * @return Current block number
     */
    @GetMapping("/block-number")
    public ResponseEntity<?> getBlockNumber() {
        return blockchainService.getCurrentBlockNumber()
            .fold(
                error -> {
                    log.error("Failed to get block number: {}", error.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                },
                blockNumber -> {
                    final Map<String, Long> response = new HashMap<>();
                    response.put("blockNumber", blockNumber);
                    return ResponseEntity.ok(response);
                }
            );
    }

    /**
     * Get current gas price
     * Complies with Rule 3.1: Single return statement
     *
     * @return Current gas price in Wei
     */
    @GetMapping("/gas-price")
    public ResponseEntity<?> getGasPrice() {
        return blockchainService.getCurrentGasPrice()
            .fold(
                error -> {
                    log.error("Failed to get gas price: {}", error.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                },
                gasPrice -> {
                    final Map<String, String> response = new HashMap<>();
                    response.put("gasPriceWei", gasPrice.toString());
                    response.put("gasPriceGwei", gasPrice.divide(BigInteger.valueOf(1_000_000_000)).toString());
                    return ResponseEntity.ok(response);
                }
            );
    }

    /**
     * Check if connected to blockchain
     * Complies with Rule 3.1: Single return statement
     *
     * @return Connection status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        final boolean connected = blockchainService.isConnected();
        final Map<String, Object> response = new HashMap<>();
        response.put("connected", connected);
        response.put("status", connected ? "CONNECTED" : "DISCONNECTED");
        return ResponseEntity.ok(response);
    }
}
