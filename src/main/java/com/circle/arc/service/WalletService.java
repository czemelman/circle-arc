package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.config.WalletProperties;
import com.circle.arc.dto.WalletInfo;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import io.vavr.control.Either;
import io.vavr.control.Try;
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
import java.util.Optional;

/**
 * Service for managing wallets on Arc blockchain
 * Compliant with coding standards: functional error handling, single return, Optional usage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final Web3j web3j;
    private final WalletProperties walletProperties;
    private final ArcBlockchainProperties blockchainProperties;

    private volatile boolean walletReady = false;
    private Credentials credentials;

    /**
     * Initialize wallet after bean construction
     * Fail-safe: errors are logged but don't prevent bean creation
     */
    @PostConstruct
    public void init() {
        final Either<ErrorInfo, Credentials> initResult = initializeWallet();

        initResult
            .peek(creds -> {
                this.credentials = creds;
                this.walletReady = true;
                log.info("Wallet initialized successfully: address={}", creds.getAddress());
            })
            .peekLeft(error -> {
                this.walletReady = false;
                log.error("Wallet initialization failed: {}. Wallet operations will be unavailable.",
                         error.getMessage());
            });
    }

    /**
     * Initialize wallet from configuration
     */
    private Either<ErrorInfo, Credentials> initializeWallet() {
        Either<ErrorInfo, Credentials> result;

        if (walletProperties.getPrivateKey() != null && !walletProperties.getPrivateKey().isEmpty()) {
            result = loadWalletFromPrivateKey(walletProperties.getPrivateKey());
        } else if (walletProperties.isEnableAutoCreate()) {
            result = createNewWallet();
        } else {
            result = Either.left(ErrorInfo.of(
                ErrorCode.WALLET_NOT_INITIALIZED,
                "No wallet configured. Set WALLET_PRIVATE_KEY or enable auto-create."
            ));
        }

        return result;
    }

    /**
     * Load wallet from private key
     */
    private Either<ErrorInfo, Credentials> loadWalletFromPrivateKey(String privateKey) {
        return Try.of(() -> {
            String cleanKey = privateKey.startsWith("0x")
                ? privateKey.substring(2)
                : privateKey;
            return Credentials.create(cleanKey);
        })
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.WALLET_CREATION_FAILED,
            "Failed to load wallet from private key",
            throwable.getMessage()
        ));
    }

    /**
     * Create a new wallet
     */
    public Either<ErrorInfo, Credentials> createNewWallet() {
        return Try.of(() -> {
            final ECKeyPair keyPair = Keys.createEcKeyPair();
            final Credentials newCredentials = Credentials.create(keyPair);

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
        })
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.WALLET_CREATION_FAILED,
            "Failed to create new wallet",
            throwable.getMessage()
        ));
    }

    /**
     * Get current wallet credentials
     * Returns Optional instead of throwing exception
     */
    public Optional<Credentials> getCredentials() {
        return Optional.ofNullable(credentials);
    }

    /**
     * Get wallet information including balance
     */
    public Either<ErrorInfo, WalletInfo> getWalletInfo() {
        Either<ErrorInfo, WalletInfo> result;

        if (!walletReady || credentials == null) {
            result = Either.left(ErrorInfo.of(
                ErrorCode.WALLET_NOT_INITIALIZED,
                "Wallet is not initialized"
            ));
        } else {
            result = fetchWalletInfo(credentials.getAddress());
        }

        return result;
    }

    /**
     * Fetch wallet information for an address
     */
    private Either<ErrorInfo, WalletInfo> fetchWalletInfo(String address) {
        return Try.of(() -> {
            final BigInteger balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance();

            final BigDecimal balance = Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);

            return WalletInfo.builder()
                .address(address)
                .balance(balance)
                .balanceWei(balanceWei.toString())
                .currency(blockchainProperties.getNativeCurrency())
                .network(blockchainProperties.getNetworkName())
                .chainId(blockchainProperties.getChainId())
                .build();
        })
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.BALANCE_FETCH_FAILED,
            "Failed to fetch wallet information",
            throwable.getMessage()
        ));
    }

    /**
     * Get balance for a specific address
     */
    public Either<ErrorInfo, BigDecimal> getBalance(String address) {
        Either<ErrorInfo, BigDecimal> result;

        if (!WalletUtils.isValidAddress(address)) {
            result = Either.left(ErrorInfo.of(
                ErrorCode.INVALID_ADDRESS,
                "Invalid Ethereum address format: " + address
            ));
        } else {
            result = fetchBalance(address);
        }

        return result;
    }

    /**
     * Fetch balance from blockchain
     */
    private Either<ErrorInfo, BigDecimal> fetchBalance(String address) {
        return Try.of(() -> {
            final BigInteger balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance();

            return Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);
        })
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.BALANCE_FETCH_FAILED,
            "Failed to fetch balance for address: " + address,
            throwable.getMessage()
        ));
    }

    /**
     * Check if wallet is initialized and ready
     */
    public boolean isWalletReady() {
        return walletReady;
    }
}
