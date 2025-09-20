# CDC Kafka Integration Project

A Change Data Capture (CDC) solution that monitors MySQL database changes and streams them to Kafka, with a consumer microservice that processes the changes and writes to a target database.

## Architecture

This project implements:
1. **Source MySQL Database**: Customer table with 10,000 records
2. **CDC with Debezium**: Monitors insert/update/delete operations on the customer table
3. **Kafka Streaming**: Streams database changes to Kafka topics
4. **Consumer Microservice**: Reads from Kafka and writes to customer_copy table

## Features

- Real-time Change Data Capture using Debezium
- Kafka-based event streaming
- Automatic data seeding (10k customer records)
- REST API for testing CRUD operations
- Docker Compose for easy infrastructure setup
- Web UI for Kafka and database monitoring

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- MySQL database (port 3306)
- Apache Kafka (port 9092)
- Zookeeper (port 2181)
- Kafka UI (port 8090)
- Adminer database UI (port 8091)

### 2. Setup Database

Connect to MySQL and run the setup script:

```bash
# Connect to MySQL
docker exec -it mysql mysql -u root -ppassword

# Run the setup script
source sql/setup.sql
```

Or use Adminer at http://localhost:8091:
- Server: mysql
- Username: root
- Password: password
- Database: customer_db

### 3. Build and Run Application

```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will:
- Start on port 8080
- Automatically seed 10,000 customer records
- Begin CDC monitoring
- Start Kafka consumer

### 4. Test the CDC Flow

#### View Kafka Topics
Visit http://localhost:8090 to see Kafka topics and messages.

#### Test CRUD Operations

```bash
# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "(555) 123-4567",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001"
  }'

# Update a customer
curl -X PUT http://localhost:8080/api/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "phone": "(555) 123-4567",
    "address": "456 Oak Ave",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90001"
  }'

# Delete a customer
curl -X DELETE http://localhost:8080/api/customers/1
```

#### Check Results

```bash
# Check customer copies
curl http://localhost:8080/api/customers/copies

# Check copy count
curl http://localhost:8080/api/customers/copies/count
```

## API Endpoints

### Customer Management
- `GET /api/customers` - List all customers
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Customer Copies (CDC Results)
- `GET /api/customers/copies` - List all customer copies
- `GET /api/customers/copies/count` - Get count of copies

## Monitoring

### Kafka UI
- URL: http://localhost:8090
- Monitor topics, messages, and consumer groups

### Database UI (Adminer)
- URL: http://localhost:8091
- Server: mysql
- Username: root
- Password: password

### Application Logs
Check application logs for CDC events and Kafka message processing.

## Database Schema

### customer table
- Primary source table with customer data
- Contains 10,000 seed records
- Monitored by Debezium CDC

### customer_copy table
- Target table for CDC results
- Includes original_id, operation_type, and processed_at fields
- Tracks INSERT, UPDATE, and DELETE operations

## Technology Stack

- **Java 17** with Spring Boot 3.2
- **MySQL 8.0** for database
- **Apache Kafka** for event streaming
- **Debezium** for Change Data Capture
- **Docker Compose** for infrastructure
- **Maven** for build management

## Troubleshooting

### Common Issues

1. **MySQL Connection Issues**
   - Ensure MySQL is running: `docker ps`
   - Check logs: `docker logs mysql`

2. **Kafka Connection Issues**
   - Verify Kafka is running: `docker ps`
   - Check logs: `docker logs kafka`

3. **CDC Not Working**
   - Ensure MySQL binary logging is enabled
   - Check Debezium logs in application output
   - Verify database permissions for debezium user

### Cleanup

```bash
# Stop all services
docker-compose down

# Remove volumes (this will delete all data)
docker-compose down -v
```

## Development

### Project Structure
```
src/main/java/com/lethargicdev/cdc/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── consumer/        # Kafka consumers
├── entity/          # JPA entities
├── repository/      # Data repositories
└── service/         # Business services
```

### Building
```bash
mvn clean compile
mvn test
mvn package
```