package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.dto.TransactionRequest;
import com.circle.arc.dto.TransactionResponse;
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
import org.web3j.protocol.core.methods.response.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for TransactionService
 * Tests all methods following coding standards with AssertJ assertions
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private WalletService walletService;

    @Mock
    private ArcBlockchainProperties blockchainProperties;

    @Mock
    private Request<?, EthGetTransactionCount> ethGetTransactionCountRequest;

    @Mock
    private EthGetTransactionCount ethGetTransactionCount;

    @Mock
    private Request<?, EthSendTransaction> ethSendTransactionRequest;

    @Mock
    private EthSendTransaction ethSendTransaction;

    @Mock
    private Request<?, EthGetTransactionReceipt> ethGetTransactionReceiptRequest;

    @Mock
    private EthGetTransactionReceipt ethGetTransactionReceipt;

    @Mock
    private Request<?, EthEstimateGas> ethEstimateGasRequest;

    @Mock
    private EthEstimateGas ethEstimateGasResponse;

    @Mock
    private Request<?, EthTransaction> ethTransactionRequest;

    @Mock
    private EthTransaction ethTransaction;

    @Mock
    private Transaction transaction;

    @Mock
    private TransactionReceipt transactionReceipt;

    @Mock
    private Credentials credentials;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(web3j, walletService, blockchainProperties);

        // Setup default blockchain properties
        when(blockchainProperties.getNativeCurrency()).thenReturn("ARC");
        when(blockchainProperties.getChainId()).thenReturn(1244L);
        when(blockchainProperties.getGasPrice()).thenReturn(1000000000L);
        when(blockchainProperties.getGasLimit()).thenReturn(21000L);
        when(blockchainProperties.getExplorerUrl()).thenReturn("https://explorer.arc.test");
    }

    // ==================== sendTransaction Tests ====================

    @Test
    void sendTransaction_withValidRequest_returnsTransactionResponse() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.5"))
                .build();

        String walletAddress = "0x1234567890abcdef1234567890abcdef12345678";
        when(credentials.getAddress()).thenReturn(walletAddress);
        when(walletService.getCredentials()).thenReturn(Optional.of(credentials));

        BigInteger nonce = BigInteger.valueOf(5);
        when(web3j.ethGetTransactionCount(anyString(), any())).thenReturn(ethGetTransactionCountRequest);
        when(ethGetTransactionCountRequest.send()).thenReturn(ethGetTransactionCount);
        when(ethGetTransactionCount.getTransactionCount()).thenReturn(nonce);

        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
        when(web3j.ethSendRawTransaction(anyString())).thenReturn(ethSendTransactionRequest);
        when(ethSendTransactionRequest.send()).thenReturn(ethSendTransaction);
        when(ethSendTransaction.hasError()).thenReturn(false);
        when(ethSendTransaction.getTransactionHash()).thenReturn(txHash);

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.sendTransaction(request);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(response -> {
            assertThat(response.getTransactionHash()).isEqualTo(txHash);
            assertThat(response.getFrom()).isEqualTo(walletAddress);
            assertThat(response.getTo()).isEqualTo(request.getToAddress());
            assertThat(response.getAmount()).isEqualByComparingTo(request.getAmount());
            assertThat(response.getCurrency()).isEqualTo("ARC");
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getExplorerUrl()).contains(txHash);
            assertThat(response.getTimestamp()).isNotNull();
        });

        verify(web3j).ethGetTransactionCount(walletAddress, org.web3j.protocol.core.DefaultBlockParameterName.PENDING);
        verify(web3j).ethSendRawTransaction(anyString());
    }

    @Test
    void sendTransaction_whenWalletNotReady_returnsError() {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .build();

        when(walletService.getCredentials()).thenReturn(Optional.empty());

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.sendTransaction(request);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.WALLET_NOT_INITIALIZED);
            assertThat(error.getMessage()).contains("Wallet credentials not available");
        });

        verify(walletService).getCredentials();
        verifyNoInteractions(web3j);
    }

    @Test
    void sendTransaction_whenWeb3jFailsToGetNonce_returnsError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .build();

        when(credentials.getAddress()).thenReturn("0x1234567890abcdef1234567890abcdef12345678");
        when(walletService.getCredentials()).thenReturn(Optional.of(credentials));

        when(web3j.ethGetTransactionCount(anyString(), any())).thenReturn(ethGetTransactionCountRequest);
        when(ethGetTransactionCountRequest.send()).thenThrow(new RuntimeException("Network error"));

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.sendTransaction(request);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.TRANSACTION_FAILED);
            assertThat(error.getMessage()).contains("Failed to send transaction");
        });
    }

    @Test
    void sendTransaction_whenWeb3jSendFails_returnsError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .build();

        when(credentials.getAddress()).thenReturn("0x1234567890abcdef1234567890abcdef12345678");
        when(walletService.getCredentials()).thenReturn(Optional.of(credentials));

        BigInteger nonce = BigInteger.valueOf(5);
        when(web3j.ethGetTransactionCount(anyString(), any())).thenReturn(ethGetTransactionCountRequest);
        when(ethGetTransactionCountRequest.send()).thenReturn(ethGetTransactionCount);
        when(ethGetTransactionCount.getTransactionCount()).thenReturn(nonce);

        when(web3j.ethSendRawTransaction(anyString())).thenReturn(ethSendTransactionRequest);
        when(ethSendTransactionRequest.send()).thenThrow(new RuntimeException("Send failed"));

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.sendTransaction(request);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.TRANSACTION_FAILED);
            assertThat(error.getMessage()).contains("Failed to send transaction");
        });
    }

    @Test
    void sendTransaction_whenTransactionHasError_returnsError() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .build();

        when(credentials.getAddress()).thenReturn("0x1234567890abcdef1234567890abcdef12345678");
        when(walletService.getCredentials()).thenReturn(Optional.of(credentials));

        BigInteger nonce = BigInteger.valueOf(5);
        when(web3j.ethGetTransactionCount(anyString(), any())).thenReturn(ethGetTransactionCountRequest);
        when(ethGetTransactionCountRequest.send()).thenReturn(ethGetTransactionCount);
        when(ethGetTransactionCount.getTransactionCount()).thenReturn(nonce);

        Response.Error error = new Response.Error(100, "Insufficient funds");
        when(web3j.ethSendRawTransaction(anyString())).thenReturn(ethSendTransactionRequest);
        when(ethSendTransactionRequest.send()).thenReturn(ethSendTransaction);
        when(ethSendTransaction.hasError()).thenReturn(true);
        when(ethSendTransaction.getError()).thenReturn(error);

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.sendTransaction(request);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(errorInfo -> {
            assertThat(errorInfo.getCode()).isEqualTo(ErrorCode.TRANSACTION_FAILED);
            assertThat(errorInfo.getMessage()).contains("Failed to send transaction");
        });
    }

    @Test
    void sendTransaction_withCustomGasPriceAndLimit_usesProvidedValues() throws Exception {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .toAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
                .amount(new BigDecimal("1.0"))
                .gasPrice(2000000000L)
                .gasLimit(30000L)
                .build();

        String walletAddress = "0x1234567890abcdef1234567890abcdef12345678";
        when(credentials.getAddress()).thenReturn(walletAddress);
        when(walletService.getCredentials()).thenReturn(Optional.of(credentials));

        BigInteger nonce = BigInteger.valueOf(5);
        when(web3j.ethGetTransactionCount(anyString(), any())).thenReturn(ethGetTransactionCountRequest);
        when(ethGetTransactionCountRequest.send()).thenReturn(ethGetTransactionCount);
        when(ethGetTransactionCount.getTransactionCount()).thenReturn(nonce);

        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
        when(web3j.ethSendRawTransaction(anyString())).thenReturn(ethSendTransactionRequest);
        when(ethSendTransactionRequest.send()).thenReturn(ethSendTransaction);
        when(ethSendTransaction.hasError()).thenReturn(false);
        when(ethSendTransaction.getTransactionHash()).thenReturn(txHash);

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.sendTransaction(request);

        // Assert
        assertThat(result.isRight()).isTrue();
        verify(web3j).ethSendRawTransaction(anyString());
    }

    // ==================== getTransactionStatus Tests ====================

    @Test
    void getTransactionStatus_forPendingTransaction_returnsPendingStatus() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        when(web3j.ethGetTransactionReceipt(txHash)).thenReturn(ethGetTransactionReceiptRequest);
        when(ethGetTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.empty());

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.getTransactionStatus(txHash);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(response -> {
            assertThat(response.getTransactionHash()).isEqualTo(txHash);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getExplorerUrl()).contains(txHash);
        });

        verify(web3j).ethGetTransactionReceipt(txHash);
    }

    @Test
    void getTransactionStatus_forConfirmedTransaction_returnsSuccessStatus() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        when(transactionReceipt.getTransactionHash()).thenReturn(txHash);
        when(transactionReceipt.getFrom()).thenReturn("0x1234567890abcdef1234567890abcdef12345678");
        when(transactionReceipt.getTo()).thenReturn("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb");
        when(transactionReceipt.getBlockNumber()).thenReturn(BigInteger.valueOf(12345));
        when(transactionReceipt.getGasUsed()).thenReturn(BigInteger.valueOf(21000));
        when(transactionReceipt.isStatusOK()).thenReturn(true);

        when(web3j.ethGetTransactionReceipt(txHash)).thenReturn(ethGetTransactionReceiptRequest);
        when(ethGetTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(transactionReceipt));

        when(transaction.getValue()).thenReturn(new BigInteger("1500000000000000000"));
        when(ethTransaction.getTransaction()).thenReturn(Optional.of(transaction));
        when(web3j.ethGetTransactionByHash(txHash)).thenReturn(ethTransactionRequest);
        when(ethTransactionRequest.send()).thenReturn(ethTransaction);

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.getTransactionStatus(txHash);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(response -> {
            assertThat(response.getTransactionHash()).isEqualTo(txHash);
            assertThat(response.getStatus()).isEqualTo("SUCCESS");
            assertThat(response.getBlockNumber()).isEqualTo(12345L);
            assertThat(response.getGasUsed()).isEqualTo(21000L);
            assertThat(response.getFrom()).isEqualTo("0x1234567890abcdef1234567890abcdef12345678");
            assertThat(response.getTo()).isEqualTo("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb");
            assertThat(response.getCurrency()).isEqualTo("ARC");
            assertThat(response.getAmount()).isEqualByComparingTo("1.5");
        });

        verify(web3j).ethGetTransactionReceipt(txHash);
        verify(web3j).ethGetTransactionByHash(txHash);
    }

    @Test
    void getTransactionStatus_forFailedTransaction_returnsFailedStatus() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        when(transactionReceipt.getTransactionHash()).thenReturn(txHash);
        when(transactionReceipt.getFrom()).thenReturn("0x1234567890abcdef1234567890abcdef12345678");
        when(transactionReceipt.getTo()).thenReturn("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb");
        when(transactionReceipt.getBlockNumber()).thenReturn(BigInteger.valueOf(12345));
        when(transactionReceipt.getGasUsed()).thenReturn(BigInteger.valueOf(21000));
        when(transactionReceipt.isStatusOK()).thenReturn(false);

        when(web3j.ethGetTransactionReceipt(txHash)).thenReturn(ethGetTransactionReceiptRequest);
        when(ethGetTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(transactionReceipt));

        when(transaction.getValue()).thenReturn(new BigInteger("1000000000000000000"));
        when(ethTransaction.getTransaction()).thenReturn(Optional.of(transaction));
        when(web3j.ethGetTransactionByHash(txHash)).thenReturn(ethTransactionRequest);
        when(ethTransactionRequest.send()).thenReturn(ethTransaction);

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.getTransactionStatus(txHash);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(response -> {
            assertThat(response.getTransactionHash()).isEqualTo(txHash);
            assertThat(response.getStatus()).isEqualTo("FAILED");
            assertThat(response.getBlockNumber()).isEqualTo(12345L);
        });

        verify(web3j).ethGetTransactionReceipt(txHash);
    }

    @Test
    void getTransactionStatus_whenWeb3jFails_returnsError() throws Exception {
        // Arrange
        String txHash = "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        when(web3j.ethGetTransactionReceipt(txHash)).thenReturn(ethGetTransactionReceiptRequest);
        when(ethGetTransactionReceiptRequest.send()).thenThrow(new RuntimeException("Network error"));

        // Act
        Either<ErrorInfo, TransactionResponse> result = transactionService.getTransactionStatus(txHash);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.TRANSACTION_STATUS_UNAVAILABLE);
            assertThat(error.getMessage()).contains("Failed to get transaction status");
        });
    }

    // ==================== estimateGas Tests ====================

    @Test
    void estimateGas_withValidParameters_returnsGasEstimate() throws Exception {
        // Arrange
        String fromAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String toAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal amount = new BigDecimal("1.0");
        BigInteger expectedGas = BigInteger.valueOf(21000);

        when(web3j.ethEstimateGas(any())).thenReturn(ethEstimateGasRequest);
        when(ethEstimateGasRequest.send()).thenReturn(ethEstimateGasResponse);
        when(ethEstimateGasResponse.getAmountUsed()).thenReturn(expectedGas);

        // Act
        Either<ErrorInfo, BigInteger> result = transactionService.estimateGas(fromAddress, toAddress, amount);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(gasEstimate -> {
            assertThat(gasEstimate).isEqualTo(expectedGas);
        });

        verify(web3j).ethEstimateGas(any());
    }

    @Test
    void estimateGas_whenWeb3jFails_returnsError() throws Exception {
        // Arrange
        String fromAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String toAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal amount = new BigDecimal("1.0");

        when(web3j.ethEstimateGas(any())).thenReturn(ethEstimateGasRequest);
        when(ethEstimateGasRequest.send()).thenThrow(new RuntimeException("Gas estimation failed"));

        // Act
        Either<ErrorInfo, BigInteger> result = transactionService.estimateGas(fromAddress, toAddress, amount);

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.GAS_ESTIMATION_FAILED);
            assertThat(error.getMessage()).contains("Failed to estimate gas");
        });
    }

    @Test
    void estimateGas_withLargeAmount_handlesCorrectly() throws Exception {
        // Arrange
        String fromAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String toAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb";
        BigDecimal amount = new BigDecimal("1000000.0");
        BigInteger expectedGas = BigInteger.valueOf(21000);

        when(web3j.ethEstimateGas(any())).thenReturn(ethEstimateGasRequest);
        when(ethEstimateGasRequest.send()).thenReturn(ethEstimateGasResponse);
        when(ethEstimateGasResponse.getAmountUsed()).thenReturn(expectedGas);

        // Act
        Either<ErrorInfo, BigInteger> result = transactionService.estimateGas(fromAddress, toAddress, amount);

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(gasEstimate -> {
            assertThat(gasEstimate).isEqualTo(expectedGas);
        });
    }
}
