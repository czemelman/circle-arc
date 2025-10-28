# Circle Arc Blockchain Spring Boot Application

A comprehensive Spring Boot application for connecting to the Circle Arc blockchain testnet and performing money movements.

## Overview

This application demonstrates how to:
- Connect to Circle Arc blockchain testnet
- Create and manage wallets
- Check account balances
- Send transactions (money movements)
- Monitor transaction status
- Interact with the Arc blockchain using Web3j

## About Circle Arc

Circle Arc is a new open Layer-1 blockchain designed specifically for stablecoin finance. Key features include:
- **Predictable Fees**: Gas fees denominated in USDC
- **Fast Finality**: Sub-second transaction finality
- **Enterprise-Grade**: Built for institutional use
- **EVM-Compatible**: Works with existing Ethereum tools

**Network Details:**
- **Network Name**: Arc Testnet
- **Chain ID**: 1244
- **RPC URL**: https://rpc-test-1.archiechain.io
- **Explorer**: https://testnet.arcscan.net
- **Native Currency**: ARC

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd circle-arc
```

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

**Option A: Run without existing wallet (auto-create)**

```bash
mvn spring-boot:run
```

The application will automatically create a new wallet and display the private key in the console. **SAVE THIS PRIVATE KEY SECURELY!**

**Option B: Run with existing wallet**

```bash
export WALLET_PRIVATE_KEY=your_private_key_here
mvn spring-boot:run
```

### 4. Verify the Application is Running

```bash
curl http://localhost:8080/actuator/health
```

## API Endpoints

### Blockchain Information

#### Get Network Info
```bash
curl http://localhost:8080/api/blockchain/network
```

**Response:**
```json
{
  "networkName": "Arc Testnet",
  "chainId": 1244,
  "rpcUrl": "https://rpc-test-1.archiechain.io",
  "blockNumber": 12345,
  "status": "CONNECTED",
  "clientVersion": "arc/v1.0.0",
  "gasPrice": "20 Gwei",
  "nativeCurrency": "ARC",
  "explorerUrl": "https://testnet.arcscan.net"
}
```

#### Get Current Block Number
```bash
curl http://localhost:8080/api/blockchain/block-number
```

#### Get Current Gas Price
```bash
curl http://localhost:8080/api/blockchain/gas-price
```

#### Check Connection Status
```bash
curl http://localhost:8080/api/blockchain/status
```

### Wallet Operations

#### Get Wallet Information
```bash
curl http://localhost:8080/api/wallet/info
```

**Response:**
```json
{
  "address": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "balance": 10.5,
  "balanceWei": "10500000000000000000",
  "currency": "ARC",
  "network": "Arc Testnet",
  "chainId": 1244
}
```

#### Get Wallet Address
```bash
curl http://localhost:8080/api/wallet/address
```

#### Get Balance for Any Address
```bash
curl http://localhost:8080/api/wallet/balance/0xYourAddressHere
```

#### Create New Wallet
```bash
curl -X POST http://localhost:8080/api/wallet/create
```

**Response:**
```json
{
  "address": "0x...",
  "privateKey": "0x...",
  "warning": "SAVE YOUR PRIVATE KEY SECURELY! This is the only time it will be displayed."
}
```

### Transaction Operations

#### Send Transaction (Money Movement)
```bash
curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d '{
    "toAddress": "0xRecipientAddressHere",
    "amount": 0.1
  }'
```

**Request Body:**
```json
{
  "toAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "amount": 0.1,
  "gasPrice": 20000000000,
  "gasLimit": 21000
}
```

**Response:**
```json
{
  "transactionHash": "0xabc123...",
  "from": "0xYourAddress",
  "to": "0xRecipientAddress",
  "amount": 0.1,
  "currency": "ARC",
  "status": "PENDING",
  "explorerUrl": "https://testnet.arcscan.net/tx/0xabc123...",
  "timestamp": 1698765432000
}
```

#### Get Transaction Status
```bash
curl http://localhost:8080/api/transaction/status/0xTransactionHashHere
```

**Response:**
```json
{
  "transactionHash": "0xabc123...",
  "from": "0xSenderAddress",
  "to": "0xRecipientAddress",
  "amount": 0.1,
  "currency": "ARC",
  "blockNumber": 12346,
  "status": "SUCCESS",
  "gasUsed": 21000,
  "explorerUrl": "https://testnet.arcscan.net/tx/0xabc123..."
}
```

#### Estimate Gas
```bash
curl "http://localhost:8080/api/transaction/estimate-gas?from=0xFrom&to=0xTo&amount=0.1"
```

## Configuration

### application.properties

Key configuration options:

```properties
# Server
server.port=8080

# Arc Blockchain
arc.blockchain.network-name=Arc Testnet
arc.blockchain.chain-id=1244
arc.blockchain.rpc-url=https://rpc-test-1.archiechain.io
arc.blockchain.explorer-url=https://testnet.arcscan.net
arc.blockchain.native-currency=ARC

# Gas Configuration
arc.blockchain.gas-price=20000000000
arc.blockchain.gas-limit=6721975

# Wallet
arc.wallet.private-key=${WALLET_PRIVATE_KEY:}
arc.wallet.enable-auto-create=true
```

### Environment Variables

- `WALLET_PRIVATE_KEY`: Your wallet's private key (without 0x prefix)

**Example:**
```bash
export WALLET_PRIVATE_KEY=1234567890abcdef...
```

## Getting Testnet Tokens

To use this application, you'll need testnet ARC tokens:

1. Get your wallet address from the API or console logs
2. Visit the Arc testnet faucet (check Circle's documentation)
3. Request testnet tokens for your address
4. Wait for the tokens to arrive (usually takes a few seconds)
5. Verify your balance using the wallet info endpoint

## Example Usage Scenarios

### Scenario 1: Check Your Balance

```bash
# Get your wallet info
curl http://localhost:8080/api/wallet/info

# Response will show your address and current balance
```

### Scenario 2: Send Money to Another Address

```bash
# Send 0.5 ARC to another address
curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d '{
    "toAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
    "amount": 0.5
  }'

# Save the transaction hash from the response
# Then check its status
curl http://localhost:8080/api/transaction/status/0xYourTransactionHash
```

### Scenario 3: Monitor Network Status

```bash
# Get complete network information
curl http://localhost:8080/api/blockchain/network

# Check current block height
curl http://localhost:8080/api/blockchain/block-number

# Check current gas prices
curl http://localhost:8080/api/blockchain/gas-price
```

## Project Structure

```
circle-arc/
├── src/
│   ├── main/
│   │   ├── java/com/circle/arc/
│   │   │   ├── ArcBlockchainApplication.java   # Main application
│   │   │   ├── config/                          # Configuration classes
│   │   │   │   ├── ArcBlockchainProperties.java
│   │   │   │   ├── WalletProperties.java
│   │   │   │   └── Web3jConfig.java
│   │   │   ├── controller/                      # REST controllers
│   │   │   │   ├── BlockchainController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── WalletController.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── dto/                             # Data transfer objects
│   │   │   │   ├── NetworkInfo.java
│   │   │   │   ├── TransactionRequest.java
│   │   │   │   ├── TransactionResponse.java
│   │   │   │   └── WalletInfo.java
│   │   │   └── service/                         # Business logic
│   │   │       ├── BlockchainService.java
│   │   │       ├── TransactionService.java
│   │   │       └── WalletService.java
│   │   └── resources/
│   │       └── application.properties           # Configuration
│   └── test/                                    # Tests
├── pom.xml                                      # Maven dependencies
└── README.md                                    # This file
```

## Technologies Used

- **Spring Boot 3.2.0**: Application framework
- **Web3j 4.11.0**: Java library for Ethereum/EVM interaction
- **Lombok**: Reduce boilerplate code
- **Bouncy Castle**: Cryptography provider

## Security Best Practices

1. **Never commit private keys** to version control
2. **Use environment variables** for sensitive data
3. **In production**, use a secure vault service (HashiCorp Vault, AWS Secrets Manager)
4. **Enable HTTPS** for production deployments
5. **Implement authentication** before deploying publicly

## Troubleshooting

### Connection Issues

If you can't connect to Arc testnet:

1. Check if the RPC endpoint is accessible:
   ```bash
   curl https://rpc-test-1.archiechain.io
   ```

2. Verify network configuration in `application.properties`

3. Check application logs for detailed error messages

### Transaction Failures

Common reasons for transaction failures:

1. **Insufficient balance**: Check your wallet balance
2. **Invalid address**: Verify the recipient address format
3. **Gas too low**: Increase gas limit or gas price
4. **Nonce issues**: Wait for pending transactions to complete

### Wallet Issues

If wallet initialization fails:

1. Verify your private key format (should be 64 hex characters)
2. Check if `WALLET_PRIVATE_KEY` environment variable is set
3. Enable auto-create to generate a new wallet

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
java -jar target/arc-blockchain-app-1.0.0.jar
```

### Adding Features

The application is modular and easy to extend:

1. **Add new endpoints**: Create controllers in `controller/` package
2. **Add business logic**: Create services in `service/` package
3. **Add data models**: Create DTOs in `dto/` package

## Resources

- [Circle Official Website](https://www.circle.com)
- [Arc Blockchain Documentation](https://arc.network)
- [Web3j Documentation](https://docs.web3j.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is for educational and demonstration purposes.

## Support

For issues and questions:
- Check the troubleshooting section
- Review Circle's Arc documentation
- Create an issue in the repository

---

**Built with Circle Arc Blockchain**
