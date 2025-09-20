#!/bin/bash

# CDC Kafka Integration Demo Script
echo "=== CDC Kafka Integration Demo ==="

BASE_URL="http://localhost:8080/api/customers"

echo "1. Checking initial customer count..."
curl -s "$BASE_URL" | jq length
echo ""

echo "2. Creating a new customer..."
CUSTOMER_DATA='{
  "firstName": "Alice",
  "lastName": "Johnson",
  "email": "alice.johnson@example.com",
  "phone": "(555) 987-6543",
  "address": "789 Pine St",
  "city": "Seattle",
  "state": "WA",
  "zipCode": "98101"
}'

CREATED_CUSTOMER=$(curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d "$CUSTOMER_DATA")

CUSTOMER_ID=$(echo "$CREATED_CUSTOMER" | jq -r '.id')
echo "Created customer with ID: $CUSTOMER_ID"
echo ""

echo "3. Waiting 2 seconds for CDC processing..."
sleep 2

echo "4. Checking customer copies (should show INSERT operation)..."
curl -s "$BASE_URL/copies" | jq '.[] | select(.originalId == '$CUSTOMER_ID')'
echo ""

echo "5. Updating the customer..."
UPDATE_DATA='{
  "firstName": "Alice",
  "lastName": "Smith",
  "email": "alice.smith@example.com", 
  "phone": "(555) 987-6543",
  "address": "456 Oak Ave",
  "city": "Portland",
  "state": "OR",
  "zipCode": "97201"
}'

curl -s -X PUT "$BASE_URL/$CUSTOMER_ID" \
  -H "Content-Type: application/json" \
  -d "$UPDATE_DATA"

echo "Updated customer $CUSTOMER_ID"
echo ""

echo "6. Waiting 2 seconds for CDC processing..."
sleep 2

echo "7. Checking customer copies (should show UPDATE operation)..."
curl -s "$BASE_URL/copies" | jq '.[] | select(.originalId == '$CUSTOMER_ID')'
echo ""

echo "8. Deleting the customer..."
curl -s -X DELETE "$BASE_URL/$CUSTOMER_ID"
echo "Deleted customer $CUSTOMER_ID"
echo ""

echo "9. Waiting 2 seconds for CDC processing..."
sleep 2

echo "10. Checking customer copies (should show DELETE operation)..."
curl -s "$BASE_URL/copies" | jq '.[] | select(.originalId == '$CUSTOMER_ID')'
echo ""

echo "11. Final customer copies count:"
curl -s "$BASE_URL/copies/count"
echo ""

echo "Demo completed! Check the application logs to see CDC events being processed."