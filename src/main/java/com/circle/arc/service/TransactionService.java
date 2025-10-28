package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.dto.TransactionRequest;
import com.circle.arc.dto.TransactionResponse;
import com.circle.arc.error.ErrorCode;
import com.circle.arc.error.ErrorInfo;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * Service for handling blockchain transactions
 * Compliant with coding standards: functional error handling, single return, Optional usage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final Web3j web3j;
    private final WalletService walletService;
    private final ArcBlockchainProperties blockchainProperties;

    /**
     * Send a transaction to transfer native currency (ARC)
     * Complies with Rule 2.1: Returns Either instead of throwing exceptions
     * Complies with Rule 2.2: Wraps external API calls
     * Complies with Rule 3.1: Single return statement
     * Complies with Rule 4.1: No .get() on Optional - uses map/orElse pattern
     */
    public Either<ErrorInfo, TransactionResponse> sendTransaction(TransactionRequest request) {
        return walletService.getCredentials()
            .map(credentials -> sendTransactionInternal(request, credentials))
            .orElse(Either.left(ErrorInfo.of(
                ErrorCode.WALLET_NOT_INITIALIZED,
                "Wallet credentials not available"
            )));
    }

    /**
     * Internal method to send transaction with credentials
     * Complies with Rule 2.2: All Web3j calls wrapped in Try
     */
    private Either<ErrorInfo, TransactionResponse> sendTransactionInternal(
            TransactionRequest request,
            Credentials credentials) {

        return Try.of(() -> {
            final String fromAddress = credentials.getAddress();

            // Convert amount to Wei
            final BigInteger amountWei = Convert.toWei(request.getAmount(), Convert.Unit.ETHER).toBigInteger();

            // Get nonce - wrapped in Try
            final BigInteger nonce = web3j.ethGetTransactionCount(
                    fromAddress,
                    org.web3j.protocol.core.DefaultBlockParameterName.PENDING)
                .send()
                .getTransactionCount();

            // Use provided gas price or default
            final BigInteger gasPrice = request.getGasPrice() != null
                    ? BigInteger.valueOf(request.getGasPrice())
                    : BigInteger.valueOf(blockchainProperties.getGasPrice());

            // Use provided gas limit or default
            final BigInteger gasLimit = request.getGasLimit() != null
                    ? BigInteger.valueOf(request.getGasLimit())
                    : BigInteger.valueOf(blockchainProperties.getGasLimit());

            // Create transaction
            final RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    request.getToAddress(),
                    amountWei
            );

            // Sign transaction
            final byte[] signedMessage = TransactionEncoder.signMessage(
                    rawTransaction,
                    blockchainProperties.getChainId(),
                    credentials
            );

            final String hexValue = Numeric.toHexString(signedMessage);

            // Send transaction
            final EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

            if (ethSendTransaction.hasError()) {
                final String errorMessage = ethSendTransaction.getError().getMessage();
                log.error("Transaction failed: {}", errorMessage);
                throw new RuntimeException("Transaction failed: " + errorMessage);
            }

            final String transactionHash = ethSendTransaction.getTransactionHash();
            log.info("Transaction sent successfully. Hash: {}", transactionHash);

            // Build response
            return TransactionResponse.builder()
                    .transactionHash(transactionHash)
                    .from(fromAddress)
                    .to(request.getToAddress())
                    .amount(request.getAmount())
                    .currency(blockchainProperties.getNativeCurrency())
                    .status("PENDING")
                    .explorerUrl(blockchainProperties.getExplorerUrl() + "/tx/" + transactionHash)
                    .timestamp(System.currentTimeMillis())
                    .build();
        })
        .onFailure(ex -> log.error("Failed to send transaction: {}", ex.getMessage(), ex))
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.TRANSACTION_FAILED,
            "Failed to send transaction",
            throwable.getMessage()
        ));
    }

    /**
     * Get transaction receipt by hash
     * Complies with Rule 2.1: Returns Either instead of throwing exceptions
     * Complies with Rule 3.1: Single return statement
     * Complies with Rule 4.1: No .get() on Optional
     */
    public Either<ErrorInfo, TransactionResponse> getTransactionStatus(String transactionHash) {
        return Try.of(() -> {
            final EthGetTransactionReceipt receiptResponse = web3j
                .ethGetTransactionReceipt(transactionHash)
                .send();

            final Optional<TransactionReceipt> receiptOptional = receiptResponse.getTransactionReceipt();

            // Use Optional.map instead of .get() - complies with Rule 4.1
            return receiptOptional
                .map(receipt -> buildTransactionResponse(transactionHash, receipt))
                .orElseGet(() -> buildPendingTransactionResponse(transactionHash));
        })
        .onFailure(ex -> log.error("Failed to get transaction status for {}: {}",
            transactionHash, ex.getMessage(), ex))
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.TRANSACTION_STATUS_UNAVAILABLE,
            "Failed to get transaction status",
            throwable.getMessage()
        ));
    }

    /**
     * Build transaction response from receipt
     * Complies with Rule 4.1: Uses Optional functional methods
     * Complies with Rule 5.3: Never returns null
     */
    private TransactionResponse buildTransactionResponse(String transactionHash, TransactionReceipt receipt) {
        // Get transaction details using Optional chaining
        final BigDecimal amount = Try.of(() -> web3j.ethGetTransactionByHash(transactionHash).send())
            .toOption()
            .flatMap(response -> response.getTransaction())
            .map(transaction -> Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER))
            .getOrElse(BigDecimal.ZERO);

        final String status = receipt.isStatusOK() ? "SUCCESS" : "FAILED";

        return TransactionResponse.builder()
                .transactionHash(transactionHash)
                .from(receipt.getFrom())
                .to(receipt.getTo())
                .amount(amount)
                .currency(blockchainProperties.getNativeCurrency())
                .blockNumber(receipt.getBlockNumber().longValue())
                .status(status)
                .gasUsed(receipt.getGasUsed().longValue())
                .explorerUrl(blockchainProperties.getExplorerUrl() + "/tx/" + transactionHash)
                .build();
    }

    /**
     * Build pending transaction response
     */
    private TransactionResponse buildPendingTransactionResponse(String transactionHash) {
        return TransactionResponse.builder()
                .transactionHash(transactionHash)
                .status("PENDING")
                .explorerUrl(blockchainProperties.getExplorerUrl() + "/tx/" + transactionHash)
                .build();
    }

    /**
     * Estimate gas for a transaction
     * Complies with Rule 2.1: Returns Either instead of throwing exceptions
     * Complies with Rule 2.2: Wraps external API calls
     * Complies with Rule 3.1: Single return statement
     */
    public Either<ErrorInfo, BigInteger> estimateGas(String from, String to, BigDecimal amount) {
        return Try.of(() -> {
            final BigInteger amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

            final org.web3j.protocol.core.methods.request.Transaction transaction =
                    org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(
                            from,
                            null,
                            null,
                            null,
                            to,
                            amountWei
                    );

            return web3j.ethEstimateGas(transaction)
                    .send()
                    .getAmountUsed();
        })
        .onFailure(ex -> log.error("Failed to estimate gas from {} to {} amount {}: {}",
            from, to, amount, ex.getMessage(), ex))
        .toEither()
        .mapLeft(throwable -> ErrorInfo.of(
            ErrorCode.GAS_ESTIMATION_FAILED,
            "Failed to estimate gas",
            throwable.getMessage()
        ));
    }
}
