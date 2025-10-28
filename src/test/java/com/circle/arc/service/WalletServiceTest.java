package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.config.WalletProperties;
import com.circle.arc.dto.WalletInfo;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for WalletService
 * Tests all methods following coding standards with AssertJ assertions
 */
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private WalletProperties walletProperties;

    @Mock
    private ArcBlockchainProperties blockchainProperties;

    @Mock
    private Request<?, EthGetBalance> ethGetBalanceRequest;

    @Mock
    private EthGetBalance ethGetBalance;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(web3j, walletProperties, blockchainProperties);

        // Setup default blockchain properties
        when(blockchainProperties.getNativeCurrency()).thenReturn("ARC");
        when(blockchainProperties.getNetworkName()).thenReturn("Arc Testnet");
        when(blockchainProperties.getChainId()).thenReturn(1244L);
    }

    // ==================== Initialization Tests ====================

    @Test
    void init_withValidPrivateKey_loadsWalletSuccessfully() {
        // Arrange
        String validPrivateKey = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        when(walletProperties.getPrivateKey()).thenReturn(validPrivateKey);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isTrue();
        assertThat(walletService.getCredentials()).isPresent();
    }

    @Test
    void init_withPrivateKeyWithout0xPrefix_loadsWalletSuccessfully() {
        // Arrange
        String validPrivateKey = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        when(walletProperties.getPrivateKey()).thenReturn(validPrivateKey);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isTrue();
        assertThat(walletService.getCredentials()).isPresent();
    }

    @Test
    void init_withInvalidPrivateKey_failsSafelyAndLogsError() {
        // Arrange
        String invalidPrivateKey = "invalid-key";
        when(walletProperties.getPrivateKey()).thenReturn(invalidPrivateKey);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isFalse();
        assertThat(walletService.getCredentials()).isEmpty();
    }

    @Test
    void init_withAutoCreateEnabled_createsNewWallet() {
        // Arrange
        when(walletProperties.getPrivateKey()).thenReturn(null);
        when(walletProperties.isEnableAutoCreate()).thenReturn(true);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isTrue();
        assertThat(walletService.getCredentials()).isPresent();
    }

    @Test
    void init_withNoConfigAndAutoCreateDisabled_failsSafely() {
        // Arrange
        when(walletProperties.getPrivateKey()).thenReturn(null);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isFalse();
        assertThat(walletService.getCredentials()).isEmpty();
    }

    // ==================== createNewWallet Tests ====================

    @Test
    void createNewWallet_success_returnsCredentialsInRight() {
        // Act
        Either<ErrorInfo, Credentials> result = walletService.createNewWallet();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(credentials -> {
            assertThat(credentials).isNotNull();
            assertThat(credentials.getAddress()).isNotNull();
            assertThat(credentials.getEcKeyPair()).isNotNull();
        });
    }

    @Test
    void createNewWallet_generatesValidEthereumAddress() {
        // Act
        Either<ErrorInfo, Credentials> result = walletService.createNewWallet();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(credentials -> {
            String address = credentials.getAddress();
            assertThat(address).startsWith("0x");
            assertThat(address).hasSize(42); // 0x + 40 hex characters
        });
    }

    // ==================== getCredentials Tests ====================

    @Test
    void getCredentials_whenWalletInitialized_returnsOptionalWithValue() {
        // Arrange
        String validPrivateKey = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        when(walletProperties.getPrivateKey()).thenReturn(validPrivateKey);
        walletService.init();

        // Act
        Optional<Credentials> result = walletService.getCredentials();

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getAddress()).isNotNull();
    }

    @Test
    void getCredentials_whenWalletNotInitialized_returnsEmptyOptional() {
        // Arrange
        when(walletProperties.getPrivateKey()).thenReturn(null);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);
        walletService.init();

        // Act
        Optional<Credentials> result = walletService.getCredentials();

        // Assert
        assertThat(result).isEmpty();
    }

    // ==================== getWalletInfo Tests ====================

    @Test
    void getWalletInfo_whenWalletReady_returnsWalletInfo() throws Exception {
        // Arrange
        String validPrivateKey = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        when(walletProperties.getPrivateKey()).thenReturn(validPrivateKey);
        walletService.init();

        BigInteger balanceWei = new BigInteger("1000000000000000000"); // 1 ARC
        when(web3j.ethGetBalance(anyString(), any())).thenReturn(ethGetBalanceRequest);
        when(ethGetBalanceRequest.send()).thenReturn(ethGetBalance);
        when(ethGetBalance.getBalance()).thenReturn(balanceWei);

        // Act
        Either<ErrorInfo, WalletInfo> result = walletService.getWalletInfo();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(walletInfo -> {
            assertThat(walletInfo.getAddress()).isNotNull();
            assertThat(walletInfo.getBalance()).isEqualByComparingTo("1.0");
            assertThat(walletInfo.getBalanceWei()).isEqualTo(balanceWei.toString());
            assertThat(walletInfo.getCurrency()).isEqualTo("ARC");
            assertThat(walletInfo.getNetwork()).isEqualTo("Arc Testnet");
            assertThat(walletInfo.getChainId()).isEqualTo(1244L);
        });
    }

    @Test
    void getWalletInfo_whenWalletNotReady_returnsError() {
        // Arrange
        when(walletProperties.getPrivateKey()).thenReturn(null);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);
        walletService.init();

        // Act
        Either<ErrorInfo, WalletInfo> result = walletService.getWalletInfo();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.WALLET_NOT_INITIALIZED);
            assertThat(error.getMessage()).contains("not initialized");
        });
    }

    @Test
    void getWalletInfo_whenWeb3jFails_returnsError() throws Exception {
        // Arrange
        String validPrivateKey = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        when(walletProperties.getPrivateKey()).thenReturn(validPrivateKey);
        walletService.init();

        when(web3j.ethGetBalance(anyString(), any())).thenReturn(ethGetBalanceRequest);
        when(ethGetBalanceRequest.send()).thenThrow(new RuntimeException("Network error"));

        // Act
        Either<ErrorInfo, WalletInfo> result = walletService.getWalletInfo();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.BALANCE_FETCH_FAILED);
            assertThat(error.getMessage()).contains("Failed to fetch wallet information");
        });
    }

    // ==================== getBalance Tests ====================

    @Test
    void getBalance_withValidAddress_returnsBalance() throws Exception {
        // Arrange
        String validAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigInteger balanceWei = new BigInteger("500000000000000000"); // 0.5 ARC

        when(web3j.ethGetBalance(anyString(), any())).thenReturn(ethGetBalanceRequest);
        when(ethGetBalanceRequest.send()).thenReturn(ethGetBalance);
        when(ethGetBalance.getBalance()).thenReturn(balanceWei);

        // Act
        Either<ErrorInfo, BigDecimal> result = walletService.getBalance(validAddress);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(balance -> {
            assertThat(balance).isEqualByComparingTo("0.5");
        });
    }

    @Test
    void getBalance_withInvalidAddress_returnsError() {
        // Arrange
        String invalidAddress = "invalid-address";

        // Act
        Either<ErrorInfo, BigDecimal> result = walletService.getBalance(invalidAddress);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.INVALID_ADDRESS);
            assertThat(error.getMessage()).contains("Invalid Ethereum address");
        });
    }

    @Test
    void getBalance_whenWeb3jFails_returnsError() throws Exception {
        // Arrange
        String validAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";

        when(web3j.ethGetBalance(anyString(), any())).thenReturn(ethGetBalanceRequest);
        when(ethGetBalanceRequest.send()).thenThrow(new RuntimeException("Network timeout"));

        // Act
        Either<ErrorInfo, BigDecimal> result = walletService.getBalance(validAddress);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.BALANCE_FETCH_FAILED);
            assertThat(error.getMessage()).contains("Failed to fetch balance");
        });
    }

    // ==================== isWalletReady Tests ====================

    @Test
    void isWalletReady_afterSuccessfulInit_returnsTrue() {
        // Arrange
        String validPrivateKey = "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        when(walletProperties.getPrivateKey()).thenReturn(validPrivateKey);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isTrue();
    }

    @Test
    void isWalletReady_afterFailedInit_returnsFalse() {
        // Arrange
        when(walletProperties.getPrivateKey()).thenReturn(null);
        when(walletProperties.isEnableAutoCreate()).thenReturn(false);

        // Act
        walletService.init();

        // Assert
        assertThat(walletService.isWalletReady()).isFalse();
    }

    @Test
    void isWalletReady_beforeInit_returnsFalse() {
        // Act & Assert
        assertThat(walletService.isWalletReady()).isFalse();
    }

    // ==================== Edge Cases ====================

    @Test
    void getBalance_withZeroBalance_returnsZero() throws Exception {
        // Arrange
        String validAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigInteger balanceWei = BigInteger.ZERO;

        when(web3j.ethGetBalance(anyString(), any())).thenReturn(ethGetBalanceRequest);
        when(ethGetBalanceRequest.send()).thenReturn(ethGetBalance);
        when(ethGetBalance.getBalance()).thenReturn(balanceWei);

        // Act
        Either<ErrorInfo, BigDecimal> result = walletService.getBalance(validAddress);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(balance -> {
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    @Test
    void getBalance_withLargeBalance_handlesCorrectly() throws Exception {
        // Arrange
        String validAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        // 1 million ARC
        BigInteger balanceWei = new BigInteger("1000000000000000000000000");

        when(web3j.ethGetBalance(anyString(), any())).thenReturn(ethGetBalanceRequest);
        when(ethGetBalanceRequest.send()).thenReturn(ethGetBalance);
        when(ethGetBalance.getBalance()).thenReturn(balanceWei);

        // Act
        Either<ErrorInfo, BigDecimal> result = walletService.getBalance(validAddress);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(balance -> {
            assertThat(balance).isEqualByComparingTo("1000000.0");
        });
    }
}
