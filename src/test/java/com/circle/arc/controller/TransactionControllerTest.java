package com.circle.arc.controller;

import com.circle.arc.dto.TransactionRequest;
import com.circle.arc.dto.TransactionResponse;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import com.circle.arc.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for TransactionController
 * Tests all endpoints using MockMvc and AssertJ assertions
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    // ==================== sendTransaction Tests ====================

    @Test
    void sendTransaction_withValidRequest_returns200WithTransactionResponse() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.5"))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .transactionHash("0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890")
                .from("0x1234567890abcdef1234567890abcdef12345678")
                .to(request.getToAddress())
                .amount(request.getAmount())
                .currency("ARC")
                .status("PENDING")
                .explorerUrl("https://explorer.arc.test/tx/0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890")
                .timestamp(System.currentTimeMillis())
                .build();

        when(transactionService.sendTransaction(any(TransactionRequest.class)))
                .thenReturn(Either.right(response));

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value("0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"))
                .andExpect(jsonPath("$.from").value("0x1234567890abcdef1234567890abcdef12345678"))
                .andExpect(jsonPath("$.to").value("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"))
                .andExpect(jsonPath("$.amount").value(1.5))
                .andExpect(jsonPath("$.currency").value("ARC"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.explorerUrl").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(transactionService).sendTransaction(any(TransactionRequest.class));
    }

    @Test
    void sendTransaction_withInvalidAddress_returns400ValidationError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("invalid-address")
                .amount(new BigDecimal("1.0"))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void sendTransaction_withMissingToAddress_returns400ValidationError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("1.0"))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void sendTransaction_withMissingAmount_returns400ValidationError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void sendTransaction_whenWalletNotReady_returns500WithError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .build();

        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.WALLET_NOT_INITIALIZED,
                "Wallet credentials not available"
        );

        when(transactionService.sendTransaction(any(TransactionRequest.class)))
                .thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.WALLET_NOT_INITIALIZED))
                .andExpect(jsonPath("$.message").value("Wallet credentials not available"));

        verify(transactionService).sendTransaction(any(TransactionRequest.class));
    }

    @Test
    void sendTransaction_whenTransactionFails_returns500WithError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .build();

        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.TRANSACTION_FAILED,
                "Failed to send transaction",
                "Insufficient funds"
        );

        when(transactionService.sendTransaction(any(TransactionRequest.class)))
                .thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.TRANSACTION_FAILED))
                .andExpect(jsonPath("$.message").value("Failed to send transaction"));

        verify(transactionService).sendTransaction(any(TransactionRequest.class));
    }

    @Test
    void sendTransaction_withCustomGasParameters_returns200() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .gasPrice(2000000000L)
                .gasLimit(30000L)
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .transactionHash("0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890")
                .from("0x1234567890abcdef1234567890abcdef12345678")
                .to(request.getToAddress())
                .amount(request.getAmount())
                .currency("ARC")
                .status("PENDING")
                .explorerUrl("https://explorer.arc.test/tx/0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890")
                .timestamp(System.currentTimeMillis())
                .build();

        when(transactionService.sendTransaction(any(TransactionRequest.class)))
                .thenReturn(Either.right(response));

        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(transactionService).sendTransaction(any(TransactionRequest.class));
    }

    // ==================== getTransactionStatus Tests ====================

    @Test
    void getTransactionStatus_forPendingTransaction_returns200WithPendingStatus() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        TransactionResponse response = TransactionResponse.builder()
                .transactionHash(txHash)
                .status("PENDING")
                .explorerUrl("https://explorer.arc.test/tx/" + txHash)
                .build();

        when(transactionService.getTransactionStatus(txHash))
                .thenReturn(Either.right(response));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/status/{transactionHash}", txHash)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value(txHash))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.explorerUrl").exists());

        verify(transactionService).getTransactionStatus(txHash);
    }

    @Test
    void getTransactionStatus_forConfirmedTransaction_returns200WithSuccessStatus() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        TransactionResponse response = TransactionResponse.builder()
                .transactionHash(txHash)
                .from("0x1234567890abcdef1234567890abcdef12345678")
                .to("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.5"))
                .currency("ARC")
                .blockNumber(12345L)
                .status("SUCCESS")
                .gasUsed(21000L)
                .explorerUrl("https://explorer.arc.test/tx/" + txHash)
                .build();

        when(transactionService.getTransactionStatus(txHash))
                .thenReturn(Either.right(response));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/status/{transactionHash}", txHash)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value(txHash))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.blockNumber").value(12345))
                .andExpect(jsonPath("$.gasUsed").value(21000))
                .andExpect(jsonPath("$.from").value("0x1234567890abcdef1234567890abcdef12345678"))
                .andExpect(jsonPath("$.to").value("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"))
                .andExpect(jsonPath("$.amount").value(1.5))
                .andExpect(jsonPath("$.currency").value("ARC"));

        verify(transactionService).getTransactionStatus(txHash);
    }

    @Test
    void getTransactionStatus_forFailedTransaction_returns200WithFailedStatus() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        TransactionResponse response = TransactionResponse.builder()
                .transactionHash(txHash)
                .from("0x1234567890abcdef1234567890abcdef12345678")
                .to("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .blockNumber(12345L)
                .status("FAILED")
                .gasUsed(21000L)
                .explorerUrl("https://explorer.arc.test/tx/" + txHash)
                .build();

        when(transactionService.getTransactionStatus(txHash))
                .thenReturn(Either.right(response));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/status/{transactionHash}", txHash)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value(txHash))
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.blockNumber").value(12345));

        verify(transactionService).getTransactionStatus(txHash);
    }

    @Test
    void getTransactionStatus_whenServiceFails_returns500WithError() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.TRANSACTION_STATUS_UNAVAILABLE,
                "Failed to get transaction status"
        );

        when(transactionService.getTransactionStatus(txHash))
                .thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/status/{transactionHash}", txHash)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.TRANSACTION_STATUS_UNAVAILABLE))
                .andExpect(jsonPath("$.message").value("Failed to get transaction status"));

        verify(transactionService).getTransactionStatus(txHash);
    }

    // ==================== estimateGas Tests ====================

    @Test
    void estimateGas_withValidParameters_returns200WithGasEstimate() throws Exception {
        // Arrange
        String fromAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String toAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal amount = new BigDecimal("1.0");
        BigInteger gasEstimate = BigInteger.valueOf(21000);

        when(transactionService.estimateGas(eq(fromAddress), eq(toAddress), eq(amount)))
                .thenReturn(Either.right(gasEstimate));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/estimate-gas")
                        .param("from", fromAddress)
                        .param("to", toAddress)
                        .param("amount", "1.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedGas").value("21000"))
                .andExpect(jsonPath("$.from").value(fromAddress))
                .andExpect(jsonPath("$.to").value(toAddress))
                .andExpect(jsonPath("$.amount").value(1.0));

        verify(transactionService).estimateGas(fromAddress, toAddress, amount);
    }

    @Test
    void estimateGas_withLargeAmount_returns200WithGasEstimate() throws Exception {
        // Arrange
        String fromAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String toAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal amount = new BigDecimal("1000000.0");
        BigInteger gasEstimate = BigInteger.valueOf(21000);

        when(transactionService.estimateGas(eq(fromAddress), eq(toAddress), eq(amount)))
                .thenReturn(Either.right(gasEstimate));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/estimate-gas")
                        .param("from", fromAddress)
                        .param("to", toAddress)
                        .param("amount", "1000000.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedGas").value("21000"))
                .andExpect(jsonPath("$.amount").value(1000000.0));

        verify(transactionService).estimateGas(fromAddress, toAddress, amount);
    }

    @Test
    void estimateGas_whenServiceFails_returns400WithError() throws Exception {
        // Arrange
        String fromAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String toAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal amount = new BigDecimal("1.0");

        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.GAS_ESTIMATION_FAILED,
                "Failed to estimate gas"
        );

        when(transactionService.estimateGas(eq(fromAddress), eq(toAddress), eq(amount)))
                .thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/estimate-gas")
                        .param("from", fromAddress)
                        .param("to", toAddress)
                        .param("amount", "1.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.GAS_ESTIMATION_FAILED))
                .andExpect(jsonPath("$.message").value("Failed to estimate gas"));

        verify(transactionService).estimateGas(fromAddress, toAddress, amount);
    }

    // ==================== Edge Cases ====================

    @Test
    void sendTransaction_withEmptyBody_returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void sendTransaction_withMalformedJson_returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/transaction/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void getTransactionStatus_withInvalidHashFormat_stillCallsService() throws Exception {
        // Arrange
        String invalidHash = "invalid-hash";

        ErrorInfo errorInfo = ErrorInfo.of(
                ErrorCode.TRANSACTION_STATUS_UNAVAILABLE,
                "Failed to get transaction status"
        );

        when(transactionService.getTransactionStatus(invalidHash))
                .thenReturn(Either.left(errorInfo));

        // Act & Assert
        mockMvc.perform(get("/api/transaction/status/{transactionHash}", invalidHash)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(transactionService).getTransactionStatus(invalidHash);
    }
}
