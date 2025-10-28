package com.circle.arc.service;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.dto.NetworkInfo;
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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;
    private final ArcBlockchainProperties blockchainProperties;

    /**
     * Get network information
     */
    public NetworkInfo getNetworkInfo() throws Exception {
        String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
        EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
        EthGasPrice gasPrice = web3j.ethGasPrice().send();

        String gasPriceGwei = Convert.fromWei(
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
    }

    /**
     * Get current block number
     */
    public Long getCurrentBlockNumber() throws Exception {
        return web3j.ethBlockNumber().send().getBlockNumber().longValue();
    }

    /**
     * Get current gas price
     */
    public BigInteger getCurrentGasPrice() throws Exception {
        return web3j.ethGasPrice().send().getGasPrice();
    }

    /**
     * Check if connected to the blockchain
     */
    public boolean isConnected() {
        try {
            web3j.web3ClientVersion().send();
            return true;
        } catch (Exception e) {
            log.error("Not connected to blockchain: {}", e.getMessage());
            return false;
        }
    }
}
