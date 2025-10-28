# Circle Arc Blockchain API Examples

This document provides detailed examples of all API endpoints with sample requests and responses.

## Table of Contents

1. [Blockchain Information APIs](#blockchain-information-apis)
2. [Wallet Management APIs](#wallet-management-apis)
3. [Transaction APIs](#transaction-apis)
4. [Complete Workflow Examples](#complete-workflow-examples)

---

## Blockchain Information APIs

### 1. Get Network Information

**Request:**
```bash
curl -X GET http://localhost:8080/api/blockchain/network
```

**Response:**
```json
{
  "networkName": "Arc Testnet",
  "chainId": 1244,
  "rpcUrl": "https://rpc-test-1.archiechain.io",
  "blockNumber": 123456,
  "status": "CONNECTED",
  "clientVersion": "arc/v1.0.0",
  "gasPrice": "20 Gwei",
  "nativeCurrency": "ARC",
  "explorerUrl": "https://testnet.arcscan.net"
}
```

### 2. Get Current Block Number

**Request:**
```bash
curl -X GET http://localhost:8080/api/blockchain/block-number
```

**Response:**
```json
{
  "blockNumber": 123456
}
```

### 3. Get Current Gas Price

**Request:**
```bash
curl -X GET http://localhost:8080/api/blockchain/gas-price
```

**Response:**
```json
{
  "gasPriceWei": "20000000000",
  "gasPriceGwei": "20"
}
```

### 4. Check Connection Status

**Request:**
```bash
curl -X GET http://localhost:8080/api/blockchain/status
```

**Response:**
```json
{
  "connected": true,
  "status": "CONNECTED"
}
```

---

## Wallet Management APIs

### 1. Get Current Wallet Information

**Request:**
```bash
curl -X GET http://localhost:8080/api/wallet/info
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

### 2. Get Wallet Address Only

**Request:**
```bash
curl -X GET http://localhost:8080/api/wallet/address
```

**Response:**
```json
{
  "address": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
}
```

### 3. Get Balance for Specific Address

**Request:**
```bash
curl -X GET http://localhost:8080/api/wallet/balance/0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb
```

**Response:**
```json
{
  "address": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "balance": 10.5,
  "currency": "ARC"
}
```

### 4. Create New Wallet

**Request:**
```bash
curl -X POST http://localhost:8080/api/wallet/create
```

**Response:**
```json
{
  "address": "0x1234567890123456789012345678901234567890",
  "privateKey": "0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
  "warning": "SAVE YOUR PRIVATE KEY SECURELY! This is the only time it will be displayed."
}
```

---

## Transaction APIs

### 1. Send Transaction (Basic)

**Request:**
```bash
curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d '{
    "toAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
    "amount": 0.1
  }'
```

**Response:**
```json
{
  "transactionHash": "0xabc123def456789abc123def456789abc123def456789abc123def456789abc1",
  "from": "0x1234567890123456789012345678901234567890",
  "to": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "amount": 0.1,
  "currency": "ARC",
  "status": "PENDING",
  "explorerUrl": "https://testnet.arcscan.net/tx/0xabc123def456789abc123def456789abc123def456789abc123def456789abc1",
  "timestamp": 1698765432000
}
```

### 2. Send Transaction (With Custom Gas)

**Request:**
```bash
curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d '{
    "toAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
    "amount": 0.5,
    "gasPrice": 25000000000,
    "gasLimit": 30000
  }'
```

**Response:**
```json
{
  "transactionHash": "0xdef456789abc123def456789abc123def456789abc123def456789abc123def4",
  "from": "0x1234567890123456789012345678901234567890",
  "to": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "amount": 0.5,
  "currency": "ARC",
  "status": "PENDING",
  "explorerUrl": "https://testnet.arcscan.net/tx/0xdef456789abc123def456789abc123def456789abc123def456789abc123def4",
  "timestamp": 1698765432000
}
```

### 3. Get Transaction Status (Pending)

**Request:**
```bash
curl -X GET http://localhost:8080/api/transaction/status/0xabc123def456789abc123def456789abc123def456789abc123def456789abc1
```

**Response:**
```json
{
  "transactionHash": "0xabc123def456789abc123def456789abc123def456789abc123def456789abc1",
  "status": "PENDING",
  "explorerUrl": "https://testnet.arcscan.net/tx/0xabc123def456789abc123def456789abc123def456789abc123def456789abc1"
}
```

### 4. Get Transaction Status (Confirmed)

**Request:**
```bash
curl -X GET http://localhost:8080/api/transaction/status/0xabc123def456789abc123def456789abc123def456789abc123def456789abc1
```

**Response:**
```json
{
  "transactionHash": "0xabc123def456789abc123def456789abc123def456789abc123def456789abc1",
  "from": "0x1234567890123456789012345678901234567890",
  "to": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "amount": 0.1,
  "currency": "ARC",
  "blockNumber": 123457,
  "status": "SUCCESS",
  "gasUsed": 21000,
  "explorerUrl": "https://testnet.arcscan.net/tx/0xabc123def456789abc123def456789abc123def456789abc123def456789abc1"
}
```

### 5. Estimate Gas for Transaction

**Request:**
```bash
curl -X GET "http://localhost:8080/api/transaction/estimate-gas?from=0x1234567890123456789012345678901234567890&to=0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb&amount=0.1"
```

**Response:**
```json
{
  "estimatedGas": "21000",
  "from": "0x1234567890123456789012345678901234567890",
  "to": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "amount": 0.1
}
```

---

## Complete Workflow Examples

### Example 1: Initial Setup and Balance Check

```bash
#!/bin/bash

# 1. Check if application is running
echo "1. Checking application health..."
curl http://localhost:8080/actuator/health
echo -e "\n"

# 2. Get network information
echo "2. Getting network information..."
curl http://localhost:8080/api/blockchain/network | jq
echo -e "\n"

# 3. Get wallet information
echo "3. Getting wallet information..."
curl http://localhost:8080/api/wallet/info | jq
echo -e "\n"

# 4. Check current block number
echo "4. Getting current block number..."
curl http://localhost:8080/api/blockchain/block-number | jq
echo -e "\n"
```

### Example 2: Complete Transaction Flow

```bash
#!/bin/bash

# Save your addresses
SENDER="YOUR_WALLET_ADDRESS"
RECIPIENT="0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"

# 1. Check balance before transaction
echo "1. Checking balance before transaction..."
curl http://localhost:8080/api/wallet/info | jq
echo -e "\n"

# 2. Estimate gas for the transaction
echo "2. Estimating gas..."
curl "http://localhost:8080/api/transaction/estimate-gas?from=$SENDER&to=$RECIPIENT&amount=0.1" | jq
echo -e "\n"

# 3. Send the transaction
echo "3. Sending transaction..."
RESPONSE=$(curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d "{
    \"toAddress\": \"$RECIPIENT\",
    \"amount\": 0.1
  }")
echo $RESPONSE | jq

# Extract transaction hash
TX_HASH=$(echo $RESPONSE | jq -r '.transactionHash')
echo "Transaction Hash: $TX_HASH"
echo -e "\n"

# 4. Wait a few seconds
echo "4. Waiting for transaction to be mined..."
sleep 5
echo -e "\n"

# 5. Check transaction status
echo "5. Checking transaction status..."
curl http://localhost:8080/api/transaction/status/$TX_HASH | jq
echo -e "\n"

# 6. Check balance after transaction
echo "6. Checking balance after transaction..."
curl http://localhost:8080/api/wallet/info | jq
echo -e "\n"
```

### Example 3: Monitor Multiple Addresses

```bash
#!/bin/bash

# Array of addresses to monitor
ADDRESSES=(
  "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
  "0x1234567890123456789012345678901234567890"
  "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"
)

echo "Monitoring balances for multiple addresses..."
echo "=============================================="

for addr in "${ADDRESSES[@]}"
do
  echo "Address: $addr"
  curl -s http://localhost:8080/api/wallet/balance/$addr | jq
  echo "----------------------------------------------"
done
```

### Example 4: Send Money to Multiple Recipients

```bash
#!/bin/bash

# Array of recipients and amounts
declare -A RECIPIENTS
RECIPIENTS["0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"]="0.1"
RECIPIENTS["0x1234567890123456789012345678901234567890"]="0.2"
RECIPIENTS["0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"]="0.15"

echo "Sending transactions to multiple recipients..."
echo "=============================================="

for addr in "${!RECIPIENTS[@]}"
do
  amount="${RECIPIENTS[$addr]}"
  echo "Sending $amount ARC to $addr..."

  curl -X POST http://localhost:8080/api/transaction/send \
    -H "Content-Type: application/json" \
    -d "{
      \"toAddress\": \"$addr\",
      \"amount\": $amount
    }" | jq

  echo "----------------------------------------------"
  sleep 2  # Wait between transactions
done
```

---

## Error Examples

### Invalid Address Format

**Request:**
```bash
curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d '{
    "toAddress": "invalid-address",
    "amount": 0.1
  }'
```

**Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-28T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "validationErrors": {
    "toAddress": "Invalid Ethereum address format"
  }
}
```

### Insufficient Balance

**Request:**
```bash
curl -X POST http://localhost:8080/api/transaction/send \
  -H "Content-Type: application/json" \
  -d '{
    "toAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
    "amount": 1000000
  }'
```

**Response (500 Internal Server Error):**
```json
{
  "timestamp": "2025-10-28T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Transaction failed: insufficient funds for gas * price + value"
}
```

---

## Testing with cURL vs Postman

### Using cURL (Command Line)

Save the examples above as shell scripts and run them:

```bash
chmod +x test-api.sh
./test-api.sh
```

### Using Postman

1. Import the following as a Postman Collection
2. Set `{{baseUrl}}` variable to `http://localhost:8080`
3. Run the requests

### Using HTTPie (Alternative)

```bash
# Install httpie
pip install httpie

# Get wallet info
http GET localhost:8080/api/wallet/info

# Send transaction
http POST localhost:8080/api/transaction/send \
  toAddress="0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb" \
  amount:=0.1
```

---

## Notes

- All amounts are in ARC (not Wei)
- Transaction hashes are 66 characters (0x + 64 hex chars)
- Addresses are 42 characters (0x + 40 hex chars)
- Gas prices are in Wei (1 Gwei = 1,000,000,000 Wei)
- Timestamps are in milliseconds since epoch

---

**Happy Testing!**
