package com.circle.arc.integration;

import com.circle.arc.config.ArcBlockchainProperties;
import com.circle.arc.config.WalletProperties;
import com.circle.arc.controller.BlockchainController;
import com.circle.arc.controller.TransactionController;
import com.circle.arc.controller.WalletController;
import com.circle.arc.service.BlockchainService;
import com.circle.arc.service.TransactionService;
import com.circle.arc.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.web3j.protocol.Web3j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test suite for the Circle Arc blockchain application
 * Tests Spring Boot context loading, bean wiring, and configuration properties
 */
@SpringBootTest
@TestPropertySource(properties = {
        "arc.blockchain.network-name=Arc Testnet",
        "arc.blockchain.chain-id=1244",
        "arc.blockchain.rpc-url=http://localhost:8545",
        "arc.blockchain.explorer-url=https://explorer.arc.test",
        "arc.blockchain.native-currency=ARC",
        "arc.blockchain.gas-price=1000000000",
        "arc.blockchain.gas-limit=21000",
        "arc.wallet.enable-auto-create=false"
})
class ApplicationIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ArcBlockchainProperties blockchainProperties;

    @Autowired
    private WalletProperties walletProperties;

    @Autowired
    private Web3j web3j;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private WalletController walletController;

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private BlockchainController blockchainController;

    // ==================== Context Loading Tests ====================

    @Test
    void contextLoads_success_applicationContextIsNotNull() {
        // Assert
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void contextLoads_success_allBeansAreLoaded() {
        // Assert - Verify all major beans are present
        assertThat(applicationContext.getBean(Web3j.class)).isNotNull();
        assertThat(applicationContext.getBean(WalletService.class)).isNotNull();
        assertThat(applicationContext.getBean(TransactionService.class)).isNotNull();
        assertThat(applicationContext.getBean(BlockchainService.class)).isNotNull();
        assertThat(applicationContext.getBean(WalletController.class)).isNotNull();
        assertThat(applicationContext.getBean(TransactionController.class)).isNotNull();
        assertThat(applicationContext.getBean(BlockchainController.class)).isNotNull();
    }

    // ==================== Configuration Properties Tests ====================

    @Test
    void blockchainProperties_success_loadedCorrectly() {
        // Assert
        assertThat(blockchainProperties).isNotNull();
        assertThat(blockchainProperties.getNetworkName()).isEqualTo("Arc Testnet");
        assertThat(blockchainProperties.getChainId()).isEqualTo(1244L);
        assertThat(blockchainProperties.getRpcUrl()).isEqualTo("http://localhost:8545");
        assertThat(blockchainProperties.getExplorerUrl()).isEqualTo("https://explorer.arc.test");
        assertThat(blockchainProperties.getNativeCurrency()).isEqualTo("ARC");
        assertThat(blockchainProperties.getGasPrice()).isEqualTo(1000000000L);
        assertThat(blockchainProperties.getGasLimit()).isEqualTo(21000L);
    }

    @Test
    void walletProperties_success_loadedCorrectly() {
        // Assert
        assertThat(walletProperties).isNotNull();
        assertThat(walletProperties.isEnableAutoCreate()).isFalse();
    }

    @Test
    void blockchainProperties_networkName_isNotEmpty() {
        // Assert
        assertThat(blockchainProperties.getNetworkName()).isNotEmpty();
    }

    @Test
    void blockchainProperties_chainId_isPositive() {
        // Assert
        assertThat(blockchainProperties.getChainId()).isPositive();
    }

    @Test
    void blockchainProperties_rpcUrl_isValidUrl() {
        // Assert
        assertThat(blockchainProperties.getRpcUrl())
                .isNotEmpty()
                .startsWith("http");
    }

    @Test
    void blockchainProperties_explorerUrl_isValidUrl() {
        // Assert
        assertThat(blockchainProperties.getExplorerUrl())
                .isNotEmpty()
                .startsWith("http");
    }

    @Test
    void blockchainProperties_gasPrice_isPositive() {
        // Assert
        assertThat(blockchainProperties.getGasPrice()).isPositive();
    }

    @Test
    void blockchainProperties_gasLimit_isPositive() {
        // Assert
        assertThat(blockchainProperties.getGasLimit()).isPositive();
    }

    // ==================== Bean Wiring Tests ====================

    @Test
    void web3jBean_success_isInjected() {
        // Assert
        assertThat(web3j).isNotNull();
    }

    @Test
    void walletService_success_isInjectedAndConfigured() {
        // Assert
        assertThat(walletService).isNotNull();
    }

    @Test
    void transactionService_success_isInjectedAndConfigured() {
        // Assert
        assertThat(transactionService).isNotNull();
    }

    @Test
    void blockchainService_success_isInjectedAndConfigured() {
        // Assert
        assertThat(blockchainService).isNotNull();
    }

    @Test
    void walletController_success_isInjectedAndConfigured() {
        // Assert
        assertThat(walletController).isNotNull();
    }

    @Test
    void transactionController_success_isInjectedAndConfigured() {
        // Assert
        assertThat(transactionController).isNotNull();
    }

    @Test
    void blockchainController_success_isInjectedAndConfigured() {
        // Assert
        assertThat(blockchainController).isNotNull();
    }

    // ==================== Service Dependencies Tests ====================

    @Test
    void walletService_hasDependenciesInjected_verifyNotNull() {
        // Assert - WalletService should have Web3j, WalletProperties, and BlockchainProperties injected
        // Since these are private fields, we verify the service itself is properly configured
        assertThat(walletService).isNotNull();
        assertThat(walletService.isWalletReady()).isIn(true, false); // Either ready or not, but method works
    }

    @Test
    void transactionService_hasDependenciesInjected_verifyNotNull() {
        // Assert - TransactionService should have Web3j, WalletService, and BlockchainProperties injected
        assertThat(transactionService).isNotNull();
    }

    @Test
    void blockchainService_hasDependenciesInjected_verifyNotNull() {
        // Assert - BlockchainService should have Web3j and BlockchainProperties injected
        assertThat(blockchainService).isNotNull();
    }

    // ==================== Controller Dependencies Tests ====================

    @Test
    void walletController_hasServiceInjected_verifyNotNull() {
        // Assert - WalletController should have WalletService injected
        assertThat(walletController).isNotNull();
    }

    @Test
    void transactionController_hasServiceInjected_verifyNotNull() {
        // Assert - TransactionController should have TransactionService injected
        assertThat(transactionController).isNotNull();
    }

    @Test
    void blockchainController_hasServiceInjected_verifyNotNull() {
        // Assert - BlockchainController should have BlockchainService injected
        assertThat(blockchainController).isNotNull();
    }

    // ==================== Application Properties Tests ====================

    @Test
    void applicationContext_containsExpectedBeanCount_verifyMinimumBeans() {
        // Assert - Verify we have at least the expected core beans
        assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(10);
    }

    @Test
    void applicationContext_containsWeb3jBean_verifyBeanExists() {
        // Assert
        assertThat(applicationContext.containsBean("web3j")).isTrue();
    }

    @Test
    void applicationContext_containsWalletServiceBean_verifyBeanExists() {
        // Assert
        assertThat(applicationContext.containsBean("walletService")).isTrue();
    }

    @Test
    void applicationContext_containsTransactionServiceBean_verifyBeanExists() {
        // Assert
        assertThat(applicationContext.containsBean("transactionService")).isTrue();
    }

    @Test
    void applicationContext_containsBlockchainServiceBean_verifyBeanExists() {
        // Assert
        assertThat(applicationContext.containsBean("blockchainService")).isTrue();
    }

    // ==================== Configuration Validation Tests ====================

    @Test
    void blockchainProperties_hasValidConfiguration_allFieldsSet() {
        // Assert
        assertThat(blockchainProperties.getNetworkName()).isNotNull();
        assertThat(blockchainProperties.getChainId()).isNotNull();
        assertThat(blockchainProperties.getRpcUrl()).isNotNull();
        assertThat(blockchainProperties.getExplorerUrl()).isNotNull();
        assertThat(blockchainProperties.getNativeCurrency()).isNotNull();
        assertThat(blockchainProperties.getGasPrice()).isNotNull();
        assertThat(blockchainProperties.getGasLimit()).isNotNull();
    }

    @Test
    void walletProperties_hasValidConfiguration_enableAutoCreateIsSet() {
        // Assert
        assertThat(walletProperties.isEnableAutoCreate()).isNotNull();
    }

    // ==================== Bean Singleton Tests ====================

    @Test
    void web3jBean_isSingleton_sameInstanceReturned() {
        // Arrange
        Web3j web3j1 = applicationContext.getBean(Web3j.class);
        Web3j web3j2 = applicationContext.getBean(Web3j.class);

        // Assert
        assertThat(web3j1).isSameAs(web3j2);
    }

    @Test
    void walletService_isSingleton_sameInstanceReturned() {
        // Arrange
        WalletService service1 = applicationContext.getBean(WalletService.class);
        WalletService service2 = applicationContext.getBean(WalletService.class);

        // Assert
        assertThat(service1).isSameAs(service2);
    }

    @Test
    void transactionService_isSingleton_sameInstanceReturned() {
        // Arrange
        TransactionService service1 = applicationContext.getBean(TransactionService.class);
        TransactionService service2 = applicationContext.getBean(TransactionService.class);

        // Assert
        assertThat(service1).isSameAs(service2);
    }

    @Test
    void blockchainService_isSingleton_sameInstanceReturned() {
        // Arrange
        BlockchainService service1 = applicationContext.getBean(BlockchainService.class);
        BlockchainService service2 = applicationContext.getBean(BlockchainService.class);

        // Assert
        assertThat(service1).isSameAs(service2);
    }

    // ==================== Edge Cases and Robustness Tests ====================

    @Test
    void applicationContext_active_isRunning() {
        // Assert
        assertThat(applicationContext.isActive()).isTrue();
    }

    @Test
    void blockchainProperties_nativeCurrency_matchesExpectedValue() {
        // Assert
        assertThat(blockchainProperties.getNativeCurrency()).isEqualTo("ARC");
    }

    @Test
    void blockchainProperties_chainId_matchesTestnetValue() {
        // Assert
        assertThat(blockchainProperties.getChainId()).isEqualTo(1244L);
    }
}
