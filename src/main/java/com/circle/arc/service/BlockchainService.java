package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.dto.NetworkInfo;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.utils.Convert;

import java.math.BigInteger;

/**
 * Service for general blockchain operations
 * Compliant with coding standards: functional error handling, single return, Optional usage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;
    private final ArcBlockchainProperties blockchainProperties;

    /**
     * Get network information
     * Complies with Rule 2.1: Returns Either instead of throwing exceptions
     * Complies with Rule 2.2: Wraps external API calls
     * Complies with Rule 3.1: Single return statement
     */
    public Either<ErrorInfo, NetworkInfo> getNetworkInfo() {
        return Try.of(() -> {
            final String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            final EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            final EthGasPrice gasPrice = web3j.ethGasPrice().send();

            final String gasPriceGwei = Convert.fromWei(
                    gasPrice.getGasPrice().toString(),
                    Convert.Unit.GWEI
            ).toString();

            return NetworkInfo.builder()
                    .networkName(blockchainProperties.getNetworkName())
                    .chainId(blockchainProperties.getChainId())
                    .rpcUrl(blockchainProperties.getRpcUrl())
                    .blockNumber(blockNumber.getBlockNumber().longValue())
                    .status("CONNECTED")
                    .clientVersion(clientVersion)
                    .gasPrice(gasPriceGwei + " Gwei")
                    .nativeCurrency(blockchainProperties.getNativeCurrency())
                    .explorerUrl(blockchainProperties.getExplorerUrl())
                    .build();
        })
        .onFailure(ex -> log.error("Failed to get network info: {}", ex.getMessage(), ex))
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.NETWORK_INFO_UNAVAILABLE,
            "Failed to get network information",
            throwable.getMessage()
        ));
    }

    /**
     * Get current block number
     * Complies with Rule 2.1: Returns Either instead of throwing exceptions
     * Complies with Rule 2.2: Wraps external API calls
     * Complies with Rule 3.1: Single return statement
     */
    public Either<ErrorInfo, Long> getCurrentBlockNumber() {
        return Try.of(() -> web3j.ethBlockNumber().send().getBlockNumber().longValue())
            .onFailure(ex -> log.error("Failed to get block number: {}", ex.getMessage(), ex))
            .toEither()
            .mapLeft(throwable -> ErrorInfo.of(
                ErrorCode.BLOCK_NUMBER_UNAVAILABLE,
                "Failed to get current block number",
                throwable.getMessage()
            ));
    }

    /**
     * Get current gas price
     * Complies with Rule 2.1: Returns Either instead of throwing exceptions
     * Complies with Rule 2.2: Wraps external API calls
     * Complies with Rule 3.1: Single return statement
     */
    public Either<ErrorInfo, BigInteger> getCurrentGasPrice() {
        return Try.of(() -> web3j.ethGasPrice().send().getGasPrice())
            .onFailure(ex -> log.error("Failed to get gas price: {}", ex.getMessage(), ex))
            .toEither()
            .mapLeft(throwable -> ErrorInfo.of(
                ErrorCode.GAS_PRICE_UNAVAILABLE,
                "Failed to get current gas price",
                throwable.getMessage()
            ));
    }

    /**
     * Check if connected to the blockchain
     * Complies with Rule 3.1: Single return statement
     */
    public boolean isConnected() {
        boolean result;

        try {
            web3j.web3ClientVersion().send();
            result = true;
        } catch (Exception e) {
            log.error("Not connected to blockchain: {}", e.getMessage());
            result = false;
        }

        return result;
    }
}
