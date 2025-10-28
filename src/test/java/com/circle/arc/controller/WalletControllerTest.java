package com.circle.arc.controller;

import com.circle.arc.dto.WalletInfo;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import com.circle.arc.service.WalletService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for WalletController
 * Tests all endpoints using MockMvc and AssertJ assertions
 */
@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    // ==================== getWalletInfo Tests ====================

    @Test
    void getWalletInfo_success_returns200WithWalletInfo() throws Exception {
        // Arrange
        WalletInfo walletInfo = WalletInfo.builder()
                .address("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .balance(new BigDecimal("10.5"))
                .balanceWei("10500000000000000000")
                .currency("ARC")
                .network("Arc Testnet")
                .chainId(1244L)
                .build();

        when(walletService.getWalletInfo()).thenReturn(Either.right(walletInfo));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"))
                .andExpect(jsonPath("$.balance").value(10.5))
                .andExpect(jsonPath("$.balanceWei").value("10500000000000000000"))
                .andExpect(jsonPath("$.currency").value("ARC"))
                .andExpect(jsonPath("$.network").value("Arc Testnet"))
                .andExpect(jsonPath("$.chainId").value(1244));

        verify(walletService).getWalletInfo();
    }

    @Test
    void getWalletInfo_whenWalletNotInitialized_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.WALLET_NOT_INITIALIZED,
                "Wallet not initialized"
        );

        when(walletService.getWalletInfo()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.WALLET_NOT_INITIALIZED))
                .andExpect(jsonPath("$.message").value("Wallet not initialized"));

        verify(walletService).getWalletInfo();
    }

    @Test
    void getWalletInfo_whenBalanceFetchFails_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.BALANCE_FETCH_FAILED,
                "Failed to fetch wallet information"
        );

        when(walletService.getWalletInfo()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.BALANCE_FETCH_FAILED))
                .andExpect(jsonPath("$.message").value("Failed to fetch wallet information"));

        verify(walletService).getWalletInfo();
    }

    // ==================== getAddress Tests ====================

    @Test
    void getAddress_success_returns200WithAddress() throws Exception {
        // Arrange
        Credentials credentials = Credentials.create(Keys.createEcKeyPair());
        String expectedAddress = credentials.getAddress();

        when(walletService.getCredentials()).thenReturn(Optional.of(credentials));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/address")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(expectedAddress));

        verify(walletService).getCredentials();
    }

    @Test
    void getAddress_whenWalletNotInitialized_returns500WithError() throws Exception {
        // Arrange
        when(walletService.getCredentials()).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/wallet/address")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_INITIALIZED"))
                .andExpect(jsonPath("$.message").value("Wallet credentials not available"));

        verify(walletService).getCredentials();
    }

    // ==================== getBalance Tests ====================

    @Test
    void getBalance_withValidAddress_returns200WithBalance() throws Exception {
        // Arrange
        String address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal balance = new BigDecimal("5.25");

        when(walletService.getBalance(address)).thenReturn(Either.right(balance));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/balance/{address}", address)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.balance").value(5.25))
                .andExpect(jsonPath("$.currency").value("ARC"));

        verify(walletService).getBalance(address);
    }

    @Test
    void getBalance_withInvalidAddress_returns400WithError() throws Exception {
        // Arrange
        String invalidAddress = "invalid-address";
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.INVALID_ADDRESS,
                "Invalid Ethereum address"
        );

        when(walletService.getBalance(invalidAddress)).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/balance/{address}", invalidAddress)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_ADDRESS))
                .andExpect(jsonPath("$.message").value("Invalid Ethereum address"));

        verify(walletService).getBalance(invalidAddress);
    }

    @Test
    void getBalance_whenFetchFails_returns400WithError() throws Exception {
        // Arrange
        String address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.BALANCE_FETCH_FAILED,
                "Failed to fetch balance"
        );

        when(walletService.getBalance(address)).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/balance/{address}", address)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BALANCE_FETCH_FAILED))
                .andExpect(jsonPath("$.message").value("Failed to fetch balance"));

        verify(walletService).getBalance(address);
    }

    @Test
    void getBalance_withZeroBalance_returns200WithZero() throws Exception {
        // Arrange
        String address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal balance = BigDecimal.ZERO;

        when(walletService.getBalance(address)).thenReturn(Either.right(balance));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/balance/{address}", address)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("ARC"));

        verify(walletService).getBalance(address);
    }

    // ==================== createWallet Tests ====================

    @Test
    void createWallet_success_returns200WithCredentials() throws Exception {
        // Arrange
        Credentials credentials = Credentials.create(Keys.createEcKeyPair());
        String expectedAddress = credentials.getAddress();

        when(walletService.createNewWallet()).thenReturn(Either.right(credentials));

        // Act & Assert
        mockMvc.perform(post("/api/wallet/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(expectedAddress))
                .andExpect(jsonPath("$.privateKey").exists())
                .andExpect(jsonPath("$.privateKey").isString())
                .andExpect(jsonPath("$.warning").value("SAVE YOUR PRIVATE KEY SECURELY! This is the only time it will be displayed."));

        verify(walletService).createNewWallet();
    }

    @Test
    void createWallet_whenCreationFails_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.WALLET_CREATION_FAILED,
                "Failed to create wallet"
        );

        when(walletService.createNewWallet()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(post("/api/wallet/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.WALLET_CREATION_FAILED))
                .andExpect(jsonPath("$.message").value("Failed to create wallet"));

        verify(walletService).createNewWallet();
    }

    @Test
    void createWallet_createsUniqueWalletEachTime_returnsUniqueAddresses() throws Exception {
        // Arrange
        Credentials credentials1 = Credentials.create(Keys.createEcKeyPair());
        Credentials credentials2 = Credentials.create(Keys.createEcKeyPair());

        when(walletService.createNewWallet())
                .thenReturn(Either.right(credentials1))
                .thenReturn(Either.right(credentials2));

        // Act & Assert - First call
        mockMvc.perform(post("/api/wallet/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(credentials1.getAddress()));

        // Act & Assert - Second call
        mockMvc.perform(post("/api/wallet/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(credentials2.getAddress()));

        verify(walletService, times(2)).createNewWallet();
    }

    // ==================== Edge Cases ====================

    @Test
    void getBalance_withMixedCaseAddress_handlesCorrectly() throws Exception {
        // Arrange
        String address = "0x742D35cC6634c0532925A3b844BC9E7595F0bEb";
        BigDecimal balance = new BigDecimal("1.0");

        when(walletService.getBalance(address)).thenReturn(Either.right(balance));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/balance/{address}", address)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(address))
                .andExpect(jsonPath("$.balance").value(1.0));

        verify(walletService).getBalance(address);
    }

    @Test
    void getWalletInfo_withLargeBalance_handlesCorrectly() throws Exception {
        // Arrange
        WalletInfo walletInfo = WalletInfo.builder()
                .address("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .balance(new BigDecimal("1000000.0"))
                .balanceWei("1000000000000000000000000")
                .currency("ARC")
                .network("Arc Testnet")
                .chainId(1244L)
                .build();

        when(walletService.getWalletInfo()).thenReturn(Either.right(walletInfo));

        // Act & Assert
        mockMvc.perform(get("/api/wallet/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000000.0))
                .andExpect(jsonPath("$.balanceWei").value("1000000000000000000000000"));

        verify(walletService).getWalletInfo();
    }
}
