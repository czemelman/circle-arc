package com.circle.arc.controller;

import com.circle.arc.dto.NetworkInfo;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import com.circle.arc.service.BlockchainService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for BlockchainController
 * Tests all endpoints using MockMvc and AssertJ assertions
 */
@WebMvcTest(BlockchainController.class)
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockchainService blockchainService;

    // ==================== getNetworkInfo Tests ====================

    @Test
    void getNetworkInfo_success_returns200WithNetworkInfo() throws Exception {
        // Arrange
        NetworkInfo networkInfo = NetworkInfo.builder()
                .networkName("Arc Testnet")
                .chainId(1244L)
                .rpcUrl("https://rpc.arc.test")
                .blockNumber(12345L)
                .status("CONNECTED")
                .clientVersion("Web3j/4.8.7")
                .gasPrice("1.0 Gwei")
                .nativeCurrency("ARC")
                .explorerUrl("https://explorer.arc.test")
                .build();

        when(blockchainService.getNetworkInfo()).thenReturn(Either.right(networkInfo));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/network")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.networkName").value("Arc Testnet"))
                .andExpect(jsonPath("$.chainId").value(1244))
                .andExpect(jsonPath("$.rpcUrl").value("https://rpc.arc.test"))
                .andExpect(jsonPath("$.blockNumber").value(12345))
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andExpect(jsonPath("$.clientVersion").value("Web3j/4.8.7"))
                .andExpect(jsonPath("$.gasPrice").value("1.0 Gwei"))
                .andExpect(jsonPath("$.nativeCurrency").value("ARC"))
                .andExpect(jsonPath("$.explorerUrl").value("https://explorer.arc.test"));

        verify(blockchainService).getNetworkInfo();
    }

    @Test
    void getNetworkInfo_whenServiceFails_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.NETWORK_INFO_UNAVAILABLE,
                "Failed to get network information"
        );

        when(blockchainService.getNetworkInfo()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/network")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.NETWORK_INFO_UNAVAILABLE))
                .andExpect(jsonPath("$.message").value("Failed to get network information"));

        verify(blockchainService).getNetworkInfo();
    }

    @Test
    void getNetworkInfo_whenConnectionFails_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.NETWORK_INFO_UNAVAILABLE,
                "Failed to get network information",
                "Connection refused"
        );

        when(blockchainService.getNetworkInfo()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/network")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.NETWORK_INFO_UNAVAILABLE))
                .andExpect(jsonPath("$.message").value("Failed to get network information"));

        verify(blockchainService).getNetworkInfo();
    }

    // ==================== getBlockNumber Tests ====================

    @Test
    void getBlockNumber_success_returns200WithBlockNumber() throws Exception {
        // Arrange
        Long blockNumber = 54321L;

        when(blockchainService.getCurrentBlockNumber()).thenReturn(Either.right(blockNumber));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/block-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockNumber").value(54321));

        verify(blockchainService).getCurrentBlockNumber();
    }

    @Test
    void getBlockNumber_whenServiceFails_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.BLOCK_NUMBER_UNAVAILABLE,
                "Failed to get current block number"
        );

        when(blockchainService.getCurrentBlockNumber()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/block-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.BLOCK_NUMBER_UNAVAILABLE))
                .andExpect(jsonPath("$.message").value("Failed to get current block number"));

        verify(blockchainService).getCurrentBlockNumber();
    }

    @Test
    void getBlockNumber_withZeroBlockNumber_returns200() throws Exception {
        // Arrange
        Long blockNumber = 0L;

        when(blockchainService.getCurrentBlockNumber()).thenReturn(Either.right(blockNumber));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/block-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockNumber").value(0));

        verify(blockchainService).getCurrentBlockNumber();
    }

    @Test
    void getBlockNumber_withLargeBlockNumber_returns200() throws Exception {
        // Arrange
        Long blockNumber = 999999999L;

        when(blockchainService.getCurrentBlockNumber()).thenReturn(Either.right(blockNumber));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/block-number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blockNumber").value(999999999));

        verify(blockchainService).getCurrentBlockNumber();
    }

    // ==================== getGasPrice Tests ====================

    @Test
    void getGasPrice_success_returns200WithGasPrice() throws Exception {
        // Arrange
        BigInteger gasPrice = new BigInteger("2000000000"); // 2 Gwei

        when(blockchainService.getCurrentGasPrice()).thenReturn(Either.right(gasPrice));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/gas-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gasPriceWei").value("2000000000"))
                .andExpect(jsonPath("$.gasPriceGwei").value("2"));

        verify(blockchainService).getCurrentGasPrice();
    }

    @Test
    void getGasPrice_whenServiceFails_returns500WithError() throws Exception {
        // Arrange
        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.GAS_PRICE_UNAVAILABLE,
                "Failed to get current gas price"
        );

        when(blockchainService.getCurrentGasPrice()).thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/gas-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.GAS_PRICE_UNAVAILABLE))
                .andExpect(jsonPath("$.message").value("Failed to get current gas price"));

        verify(blockchainService).getCurrentGasPrice();
    }

    @Test
    void getGasPrice_withZeroGasPrice_returns200() throws Exception {
        // Arrange
        BigInteger gasPrice = BigInteger.ZERO;

        when(blockchainService.getCurrentGasPrice()).thenReturn(Either.right(gasPrice));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/gas-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gasPriceWei").value("0"))
                .andExpect(jsonPath("$.gasPriceGwei").value("0"));

        verify(blockchainService).getCurrentGasPrice();
    }

    @Test
    void getGasPrice_withHighGasPrice_returns200AndConvertsCorrectly() throws Exception {
        // Arrange
        BigInteger gasPrice = new BigInteger("100000000000"); // 100 Gwei

        when(blockchainService.getCurrentGasPrice()).thenReturn(Either.right(gasPrice));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/gas-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gasPriceWei").value("100000000000"))
                .andExpect(jsonPath("$.gasPriceGwei").value("100"));

        verify(blockchainService).getCurrentGasPrice();
    }

    @Test
    void getGasPrice_withFractionalGwei_returns200AndConvertsCorrectly() throws Exception {
        // Arrange
        BigInteger gasPrice = new BigInteger("1500000000"); // 1.5 Gwei

        when(blockchainService.getCurrentGasPrice()).thenReturn(Either.right(gasPrice));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/gas-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gasPriceWei").value("1500000000"))
                .andExpect(jsonPath("$.gasPriceGwei").value("1"));

        verify(blockchainService).getCurrentGasPrice();
    }

    // ==================== getStatus Tests ====================

    @Test
    void getStatus_whenConnected_returns200WithConnectedStatus() throws Exception {
        // Arrange
        when(blockchainService.isConnected()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.status").value("CONNECTED"));

        verify(blockchainService).isConnected();
    }

    @Test
    void getStatus_whenDisconnected_returns200WithDisconnectedStatus() throws Exception {
        // Arrange
        when(blockchainService.isConnected()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.status").value("DISCONNECTED"));

        verify(blockchainService).isConnected();
    }

    @Test
    void getStatus_alwaysReturns200_evenWhenDisconnected() throws Exception {
        // Arrange
        when(blockchainService.isConnected()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(blockchainService).isConnected();
    }

    // ==================== Edge Cases ====================

    @Test
    void getNetworkInfo_withAllFieldsPopulated_returns200WithCompleteInfo() throws Exception {
        // Arrange
        NetworkInfo networkInfo = NetworkInfo.builder()
                .networkName("Arc Mainnet")
                .chainId(1)
                .rpcUrl("https://mainnet.arc.io")
                .blockNumber(99999999L)
                .status("CONNECTED")
                .clientVersion("Web3j/5.0.0")
                .gasPrice("50.5 Gwei")
                .nativeCurrency("ARC")
                .explorerUrl("https://explorer.arc.io")
                .build();

        when(blockchainService.getNetworkInfo()).thenReturn(Either.right(networkInfo));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/network")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.networkName").value("Arc Mainnet"))
                .andExpect(jsonPath("$.chainId").value(1))
                .andExpect(jsonPath("$.rpcUrl").value("https://mainnet.arc.io"))
                .andExpect(jsonPath("$.blockNumber").value(99999999))
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andExpect(jsonPath("$.clientVersion").value("Web3j/5.0.0"))
                .andExpect(jsonPath("$.gasPrice").value("50.5 Gwei"))
                .andExpect(jsonPath("$.nativeCurrency").value("ARC"))
                .andExpect(jsonPath("$.explorerUrl").value("https://explorer.arc.io"));

        verify(blockchainService).getNetworkInfo();
    }

    @Test
    void multipleRequests_toGetStatus_returnsConsistentResults() throws Exception {
        // Arrange
        when(blockchainService.isConnected()).thenReturn(true);

        // Act & Assert - First request
        mockMvc.perform(get("/api/blockchain/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.status").value("CONNECTED"));

        // Act & Assert - Second request
        mockMvc.perform(get("/api/blockchain/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.status").value("CONNECTED"));

        verify(blockchainService, times(2)).isConnected();
    }

    @Test
    void getGasPrice_withVeryLargeGasPrice_handlesCorrectly() throws Exception {
        // Arrange
        BigInteger gasPrice = new BigInteger("1000000000000"); // 1000 Gwei

        when(blockchainService.getCurrentGasPrice()).thenReturn(Either.right(gasPrice));

        // Act & Assert
        mockMvc.perform(get("/api/blockchain/gas-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gasPriceWei").value("1000000000000"))
                .andExpect(jsonPath("$.gasPriceGwei").value("1000"));

        verify(blockchainService).getCurrentGasPrice();
    }
}
