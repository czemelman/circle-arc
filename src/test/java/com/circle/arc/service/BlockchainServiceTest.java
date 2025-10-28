package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.dto.NetworkInfo;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for BlockchainService
 * Tests all methods following coding standards with AssertJ assertions
 */
@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private ArcBlockchainProperties blockchainProperties;

    @Mock
    private Request<?, Web3ClientVersion> web3ClientVersionRequest;

    @Mock
    private Web3ClientVersion web3ClientVersion;

    @Mock
    private Request<?, EthBlockNumber> ethBlockNumberRequest;

    @Mock
    private EthBlockNumber ethBlockNumber;

    @Mock
    private Request<?, EthGasPrice> ethGasPriceRequest;

    @Mock
    private EthGasPrice ethGasPrice;

    private BlockchainService blockchainService;

    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainService(web3j, blockchainProperties);

        // Setup default blockchain properties
        when(blockchainProperties.getNetworkName()).thenReturn("Arc Testnet");
        when(blockchainProperties.getChainId()).thenReturn(1244L);
        when(blockchainProperties.getRpcUrl()).thenReturn("https://rpc.arc.test");
        when(blockchainProperties.getNativeCurrency()).thenReturn("ARC");
        when(blockchainProperties.getExplorerUrl()).thenReturn("https://explorer.arc.test");
    }

    // ==================== getNetworkInfo Tests ====================

    @Test
    void getNetworkInfo_success_returnsNetworkInfo() throws Exception {
        // Arrange
        String clientVersion = "Web3j/4.8.7";
        BigInteger blockNumber = BigInteger.valueOf(12345);
        BigInteger gasPriceWei = new BigInteger("1000000000"); // 1 Gwei

        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenReturn(web3ClientVersion);
        when(web3ClientVersion.getWeb3ClientVersion()).thenReturn(clientVersion);

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(blockNumber);

        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenReturn(ethGasPrice);
        when(ethGasPrice.getGasPrice()).thenReturn(gasPriceWei);

        // Act
        Either<ErrorInfo, NetworkInfo> result = blockchainService.getNetworkInfo();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(networkInfo -> {
            assertThat(networkInfo.getNetworkName()).isEqualTo("Arc Testnet");
            assertThat(networkInfo.getChainId()).isEqualTo(1244L);
            assertThat(networkInfo.getRpcUrl()).isEqualTo("https://rpc.arc.test");
            assertThat(networkInfo.getBlockNumber()).isEqualTo(12345L);
            assertThat(networkInfo.getStatus()).isEqualTo("CONNECTED");
            assertThat(networkInfo.getClientVersion()).isEqualTo(clientVersion);
            assertThat(networkInfo.getGasPrice()).contains("Gwei");
            assertThat(networkInfo.getNativeCurrency()).isEqualTo("ARC");
            assertThat(networkInfo.getExplorerUrl()).isEqualTo("https://explorer.arc.test");
        });

        verify(web3j).web3ClientVersion();
        verify(web3j).ethBlockNumber();
        verify(web3j).ethGasPrice();
    }

    @Test
    void getNetworkInfo_whenClientVersionFails_returnsError() throws Exception {
        // Arrange
        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenThrow(new RuntimeException("Connection refused"));

        // Act
        Either<ErrorInfo, NetworkInfo> result = blockchainService.getNetworkInfo();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.NETWORK_INFO_UNAVAILABLE);
            assertThat(error.getMessage()).contains("Failed to get network information");
        });

        verify(web3j).web3ClientVersion();
    }

    @Test
    void getNetworkInfo_whenBlockNumberFails_returnsError() throws Exception {
        // Arrange
        String clientVersion = "Web3j/4.8.7";
        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenReturn(web3ClientVersion);
        when(web3ClientVersion.getWeb3ClientVersion()).thenReturn(clientVersion);

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenThrow(new RuntimeException("Block number unavailable"));

        // Act
        Either<ErrorInfo, NetworkInfo> result = blockchainService.getNetworkInfo();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.NETWORK_INFO_UNAVAILABLE);
            assertThat(error.getMessage()).contains("Failed to get network information");
        });
    }

    @Test
    void getNetworkInfo_whenGasPriceFails_returnsError() throws Exception {
        // Arrange
        String clientVersion = "Web3j/4.8.7";
        BigInteger blockNumber = BigInteger.valueOf(12345);

        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenReturn(web3ClientVersion);
        when(web3ClientVersion.getWeb3ClientVersion()).thenReturn(clientVersion);

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(blockNumber);

        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenThrow(new RuntimeException("Gas price unavailable"));

        // Act
        Either<ErrorInfo, NetworkInfo> result = blockchainService.getNetworkInfo();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.NETWORK_INFO_UNAVAILABLE);
            assertThat(error.getMessage()).contains("Failed to get network information");
        });
    }

    @Test
    void getNetworkInfo_withHighGasPrice_formatsCorrectly() throws Exception {
        // Arrange
        String clientVersion = "Web3j/4.8.7";
        BigInteger blockNumber = BigInteger.valueOf(12345);
        BigInteger gasPriceWei = new BigInteger("50000000000"); // 50 Gwei

        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenReturn(web3ClientVersion);
        when(web3ClientVersion.getWeb3ClientVersion()).thenReturn(clientVersion);

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(blockNumber);

        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenReturn(ethGasPrice);
        when(ethGasPrice.getGasPrice()).thenReturn(gasPriceWei);

        // Act
        Either<ErrorInfo, NetworkInfo> result = blockchainService.getNetworkInfo();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(networkInfo -> {
            assertThat(networkInfo.getGasPrice()).contains("Gwei");
            assertThat(networkInfo.getGasPrice()).contains("50");
        });
    }

    // ==================== getCurrentBlockNumber Tests ====================

    @Test
    void getCurrentBlockNumber_success_returnsBlockNumber() throws Exception {
        // Arrange
        BigInteger blockNumber = BigInteger.valueOf(54321);

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(blockNumber);

        // Act
        Either<ErrorInfo, Long> result = blockchainService.getCurrentBlockNumber();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(blockNum -> {
            assertThat(blockNum).isEqualTo(54321L);
        });

        verify(web3j).ethBlockNumber();
    }

    @Test
    void getCurrentBlockNumber_whenWeb3jFails_returnsError() throws Exception {
        // Arrange
        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenThrow(new RuntimeException("Network timeout"));

        // Act
        Either<ErrorInfo, Long> result = blockchainService.getCurrentBlockNumber();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.BLOCK_NUMBER_UNAVAILABLE);
            assertThat(error.getMessage()).contains("Failed to get current block number");
        });

        verify(web3j).ethBlockNumber();
    }

    @Test
    void getCurrentBlockNumber_withZeroBlockNumber_handlesCorrectly() throws Exception {
        // Arrange
        BigInteger blockNumber = BigInteger.ZERO;

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(blockNumber);

        // Act
        Either<ErrorInfo, Long> result = blockchainService.getCurrentBlockNumber();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(blockNum -> {
            assertThat(blockNum).isEqualTo(0L);
        });
    }

    @Test
    void getCurrentBlockNumber_withLargeBlockNumber_handlesCorrectly() throws Exception {
        // Arrange
        BigInteger blockNumber = new BigInteger("999999999");

        when(web3j.ethBlockNumber()).thenReturn(ethBlockNumberRequest);
        when(ethBlockNumberRequest.send()).thenReturn(ethBlockNumber);
        when(ethBlockNumber.getBlockNumber()).thenReturn(blockNumber);

        // Act
        Either<ErrorInfo, Long> result = blockchainService.getCurrentBlockNumber();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(blockNum -> {
            assertThat(blockNum).isEqualTo(999999999L);
        });
    }

    // ==================== getCurrentGasPrice Tests ====================

    @Test
    void getCurrentGasPrice_success_returnsGasPrice() throws Exception {
        // Arrange
        BigInteger gasPrice = new BigInteger("2000000000"); // 2 Gwei

        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenReturn(ethGasPrice);
        when(ethGasPrice.getGasPrice()).thenReturn(gasPrice);

        // Act
        Either<ErrorInfo, BigInteger> result = blockchainService.getCurrentGasPrice();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(price -> {
            assertThat(price).isEqualTo(new BigInteger("2000000000"));
        });

        verify(web3j).ethGasPrice();
    }

    @Test
    void getCurrentGasPrice_whenWeb3jFails_returnsError() throws Exception {
        // Arrange
        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenThrow(new RuntimeException("Gas price service unavailable"));

        // Act
        Either<ErrorInfo, BigInteger> result = blockchainService.getCurrentGasPrice();

        // Assert
        assertThat(result.isLeft()).isTrue();
        result.peekLeft(error -> {
            assertThat(error.getCode()).isEqualTo(ErrorCode.GAS_PRICE_UNAVAILABLE);
            assertThat(error.getMessage()).contains("Failed to get current gas price");
        });

        verify(web3j).ethGasPrice();
    }

    @Test
    void getCurrentGasPrice_withZeroGasPrice_handlesCorrectly() throws Exception {
        // Arrange
        BigInteger gasPrice = BigInteger.ZERO;

        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenReturn(ethGasPrice);
        when(ethGasPrice.getGasPrice()).thenReturn(gasPrice);

        // Act
        Either<ErrorInfo, BigInteger> result = blockchainService.getCurrentGasPrice();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(price -> {
            assertThat(price).isEqualTo(BigInteger.ZERO);
        });
    }

    @Test
    void getCurrentGasPrice_withHighGasPrice_handlesCorrectly() throws Exception {
        // Arrange
        BigInteger gasPrice = new BigInteger("100000000000"); // 100 Gwei

        when(web3j.ethGasPrice()).thenReturn(ethGasPriceRequest);
        when(ethGasPriceRequest.send()).thenReturn(ethGasPrice);
        when(ethGasPrice.getGasPrice()).thenReturn(gasPrice);

        // Act
        Either<ErrorInfo, BigInteger> result = blockchainService.getCurrentGasPrice();

        // Assert
        assertThat(result.isRight()).isTrue();
        result.peek(price -> {
            assertThat(price).isEqualTo(new BigInteger("100000000000"));
        });
    }

    // ==================== isConnected Tests ====================

    @Test
    void isConnected_whenConnected_returnsTrue() throws Exception {
        // Arrange
        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenReturn(web3ClientVersion);
        when(web3ClientVersion.getWeb3ClientVersion()).thenReturn("Web3j/4.8.7");

        // Act
        boolean result = blockchainService.isConnected();

        // Assert
        assertThat(result).isTrue();
        verify(web3j).web3ClientVersion();
    }

    @Test
    void isConnected_whenDisconnected_returnsFalse() throws Exception {
        // Arrange
        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenThrow(new RuntimeException("Connection failed"));

        // Act
        boolean result = blockchainService.isConnected();

        // Assert
        assertThat(result).isFalse();
        verify(web3j).web3ClientVersion();
    }

    @Test
    void isConnected_whenTimeoutOccurs_returnsFalse() throws Exception {
        // Arrange
        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenThrow(new RuntimeException("Timeout"));

        // Act
        boolean result = blockchainService.isConnected();

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isConnected_whenIOException_returnsFalse() throws Exception {
        // Arrange
        when(web3j.web3ClientVersion()).thenReturn(web3ClientVersionRequest);
        when(web3ClientVersionRequest.send()).thenThrow(new RuntimeException("IO error"));

        // Act
        boolean result = blockchainService.isConnected();

        // Assert
        assertThat(result).isFalse();
    }
}
