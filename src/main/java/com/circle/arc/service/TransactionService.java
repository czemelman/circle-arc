package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.dto.TransactionRequest;
import com.circle.arc.dto.TransactionResponse;
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
     */
    public TransactionResponse sendTransaction(TransactionRequest request) throws Exception {
        Credentials credentials = walletService.getCredentials();
        String fromAddress = credentials.getAddress();

        // Convert amount to Wei
        BigInteger amountWei = Convert.toWei(request.getAmount(), Convert.Unit.ETHER).toBigInteger();

        // Get nonce
        BigInteger nonce = web3j.ethGetTransactionCount(fromAddress,
                org.web3j.protocol.core.DefaultBlockParameterName.PENDING)
                .send()
                .getTransactionCount();

        // Use provided gas price or default
        BigInteger gasPrice = request.getGasPrice() != null
                ? BigInteger.valueOf(request.getGasPrice())
                : BigInteger.valueOf(blockchainProperties.getGasPrice());

        // Use provided gas limit or default
        BigInteger gasLimit = request.getGasLimit() != null
                ? BigInteger.valueOf(request.getGasLimit())
                : BigInteger.valueOf(blockchainProperties.getGasLimit());

        // Create transaction
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                request.getToAddress(),
                amountWei
        );

        // Sign transaction
        byte[] signedMessage = TransactionEncoder.signMessage(
                rawTransaction,
                blockchainProperties.getChainId(),
                credentials
        );

        String hexValue = Numeric.toHexString(signedMessage);

        // Send transaction
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction.hasError()) {
            String errorMessage = ethSendTransaction.getError().getMessage();
            log.error("Transaction failed: {}", errorMessage);
            throw new RuntimeException("Transaction failed: " + errorMessage);
        }

        String transactionHash = ethSendTransaction.getTransactionHash();
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
    }

    /**
     * Get transaction receipt by hash
     */
    public TransactionResponse getTransactionStatus(String transactionHash) throws Exception {
        EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(transactionHash)
                .send();

        Optional<TransactionReceipt> receiptOptional = receiptResponse.getTransactionReceipt();

        if (receiptOptional.isEmpty()) {
            return TransactionResponse.builder()
                    .transactionHash(transactionHash)
                    .status("PENDING")
                    .explorerUrl(blockchainProperties.getExplorerUrl() + "/tx/" + transactionHash)
                    .build();
        }

        TransactionReceipt receipt = receiptOptional.get();

        // Get transaction details
        org.web3j.protocol.core.methods.response.Transaction transaction =
                web3j.ethGetTransactionByHash(transactionHash)
                        .send()
                        .getTransaction()
                        .orElse(null);

        BigDecimal amount = transaction != null
                ? Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER)
                : BigDecimal.ZERO;

        String status = receipt.isStatusOK() ? "SUCCESS" : "FAILED";

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
     * Estimate gas for a transaction
     */
    public BigInteger estimateGas(String from, String to, BigDecimal amount) throws Exception {
        BigInteger amountWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

        org.web3j.protocol.core.methods.request.Transaction transaction =
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
    }
}
