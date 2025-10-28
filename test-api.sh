#!/bin/bash

# Circle Arc Blockchain Application - API Test Script
# This script demonstrates the basic functionality of the application

BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Circle Arc Blockchain API Test Script${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Function to print step
print_step() {
    echo -e "${GREEN}$1${NC}"
}

# Function to wait for user
wait_for_user() {
    echo ""
    read -p "Press Enter to continue..."
    echo ""
}

# 1. Check application health
print_step "Step 1: Checking application health..."
curl -s $BASE_URL/actuator/health | jq
wait_for_user

# 2. Get network information
print_step "Step 2: Getting Arc Testnet network information..."
curl -s $BASE_URL/api/blockchain/network | jq
wait_for_user

# 3. Get wallet information
print_step "Step 3: Getting wallet information..."
WALLET_INFO=$(curl -s $BASE_URL/api/wallet/info)
echo $WALLET_INFO | jq
WALLET_ADDRESS=$(echo $WALLET_INFO | jq -r '.address')
WALLET_BALANCE=$(echo $WALLET_INFO | jq -r '.balance')
echo ""
echo -e "${BLUE}Your Wallet Address: $WALLET_ADDRESS${NC}"
echo -e "${BLUE}Your Balance: $WALLET_BALANCE ARC${NC}"
wait_for_user

# 4. Check current block number
print_step "Step 4: Getting current block number..."
curl -s $BASE_URL/api/blockchain/block-number | jq
wait_for_user

# 5. Get current gas price
print_step "Step 5: Getting current gas price..."
curl -s $BASE_URL/api/blockchain/gas-price | jq
wait_for_user

# 6. Check connection status
print_step "Step 6: Checking blockchain connection status..."
curl -s $BASE_URL/api/blockchain/status | jq
wait_for_user

# 7. Ask user if they want to send a transaction
echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}Transaction Test (Optional)${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo "Current balance: $WALLET_BALANCE ARC"
echo ""
read -p "Do you want to send a test transaction? (y/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo ""
    read -p "Enter recipient address (0x...): " RECIPIENT
    read -p "Enter amount to send: " AMOUNT

    # Validate inputs
    if [[ -z "$RECIPIENT" ]] || [[ -z "$AMOUNT" ]]; then
        echo -e "${RED}Error: Recipient address and amount are required${NC}"
        exit 1
    fi

    print_step "Step 7: Estimating gas for transaction..."
    curl -s "$BASE_URL/api/transaction/estimate-gas?from=$WALLET_ADDRESS&to=$RECIPIENT&amount=$AMOUNT" | jq
    wait_for_user

    print_step "Step 8: Sending transaction..."
    TX_RESPONSE=$(curl -s -X POST $BASE_URL/api/transaction/send \
        -H "Content-Type: application/json" \
        -d "{
            \"toAddress\": \"$RECIPIENT\",
            \"amount\": $AMOUNT
        }")

    echo $TX_RESPONSE | jq
    TX_HASH=$(echo $TX_RESPONSE | jq -r '.transactionHash')

    if [[ "$TX_HASH" != "null" ]] && [[ -n "$TX_HASH" ]]; then
        echo ""
        echo -e "${GREEN}Transaction sent successfully!${NC}"
        echo -e "${BLUE}Transaction Hash: $TX_HASH${NC}"
        wait_for_user

        print_step "Step 9: Waiting for transaction to be mined (5 seconds)..."
        sleep 5

        print_step "Step 10: Checking transaction status..."
        curl -s $BASE_URL/api/transaction/status/$TX_HASH | jq
        wait_for_user

        print_step "Step 11: Checking updated balance..."
        curl -s $BASE_URL/api/wallet/info | jq
    else
        echo -e "${RED}Transaction failed! Check the error above.${NC}"
    fi
fi

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}API Test Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo "For more examples, see API_EXAMPLES.md"
echo ""
