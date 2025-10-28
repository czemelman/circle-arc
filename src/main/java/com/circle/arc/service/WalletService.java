package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.config.WalletProperties;
import com.circle.arc.dto.WalletInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Service for managing wallets on Arc blockchain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final Web3j web3j;
    private final WalletProperties walletProperties;
    private final ArcBlockchainProperties blockchainProperties;

    private Credentials credentials;

    /**
     * Initialize wallet after bean construction
     */
    @PostConstruct
    public void init() {
        try {
            if (walletProperties.getPrivateKey() != null && !walletProperties.getPrivateKey().isEmpty()) {
                // Load existing wallet from private key
                String privateKey = walletProperties.getPrivateKey();
                if (privateKey.startsWith("0x")) {
                    privateKey = privateKey.substring(2);
                }
                credentials = Credentials.create(privateKey);
                log.info("Loaded wallet with address: {}", credentials.getAddress());
            } else if (walletProperties.isEnableAutoCreate()) {
                // Create new wallet
                credentials = createNewWallet();
                log.info("Created new wallet with address: {}", credentials.getAddress());
                log.warn("IMPORTANT: Save your private key: 0x{}", credentials.getEcKeyPair().getPrivateKey().toString(16));
            } else {
                log.warn("No wallet configured. Set WALLET_PRIVATE_KEY environment variable or enable auto-create.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize wallet: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a new wallet
     */
    public Credentials createNewWallet() throws Exception {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        Credentials newCredentials = Credentials.create(keyPair);

        log.info("===========================================");
        log.info("NEW WALLET CREATED");
        log.info("===========================================");
        log.info("Address: {}", newCredentials.getAddress());
        log.info("Private Key: 0x{}", keyPair.getPrivateKey().toString(16));
        log.info("Public Key: 0x{}", keyPair.getPublicKey().toString(16));
        log.info("===========================================");
        log.warn("SAVE YOUR PRIVATE KEY SECURELY!");
        log.warn("You will need testnet ARC tokens to make transactions.");
        log.info("===========================================");

        return newCredentials;
    }

    /**
     * Get current wallet credentials
     */
    public Credentials getCredentials() {
        if (credentials == null) {
            throw new IllegalStateException("Wallet not initialized");
        }
        return credentials;
    }

    /**
     * Get wallet information including balance
     */
    public WalletInfo getWalletInfo() throws Exception {
        if (credentials == null) {
            throw new IllegalStateException("Wallet not initialized");
        }

        String address = credentials.getAddress();
        BigInteger balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance();

        BigDecimal balance = Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);

        return WalletInfo.builder()
                .address(address)
                .balance(balance)
                .balanceWei(balanceWei.toString())
                .currency(blockchainProperties.getNativeCurrency())
                .network(blockchainProperties.getNetworkName())
                .chainId(blockchainProperties.getChainId())
                .build();
    }

    /**
     * Get balance for a specific address
     */
    public BigDecimal getBalance(String address) throws Exception {
        if (!WalletUtils.isValidAddress(address)) {
            throw new IllegalArgumentException("Invalid address format");
        }

        BigInteger balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance();

        return Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);
    }

    /**
     * Check if wallet is initialized
     */
    public boolean isWalletInitialized() {
        return credentials != null;
    }
}
