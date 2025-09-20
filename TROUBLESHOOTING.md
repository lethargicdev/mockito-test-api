# Troubleshooting Guide

## Common Issues and Solutions

### 1. MySQL Connection Issues

**Symptoms:**
- "Connection refused" errors
- "Access denied" errors

**Solutions:**
```bash
# Check if MySQL container is running
docker ps | grep mysql

# Check MySQL logs
docker logs mysql

# Connect manually to test
docker exec -it mysql mysql -u root -ppassword

# Reset MySQL if needed
docker compose down
docker volume rm mockito-test-api_mysql_data
docker compose up -d mysql
```

### 2. Kafka Connection Issues

**Symptoms:**
- "Connection to node -1 could not be established"
- Timeout errors when connecting to Kafka

**Solutions:**
```bash
# Check if Kafka containers are running
docker ps | grep -E "(kafka|zookeeper)"

# Check Kafka logs
docker logs kafka
docker logs zookeeper

# Test Kafka connection
docker exec kafka kafka-topics --bootstrap-server localhost:29092 --list

# Restart Kafka services
docker compose restart zookeeper kafka
```

### 3. Debezium CDC Issues

**Symptoms:**
- No CDC events being generated
- Debezium startup errors

**Solutions:**
```bash
# Check MySQL binary logging is enabled
docker exec mysql mysql -u root -ppassword -e "SHOW VARIABLES LIKE '%log_bin%'"

# Verify debezium user permissions  
docker exec mysql mysql -u root -ppassword -e "SHOW GRANTS FOR 'debezium'@'%'"

# Check if tables exist
docker exec mysql mysql -u root -ppassword customer_db -e "SHOW TABLES"

# Enable debug logging
# Add to application.yml: logging.level.io.debezium: DEBUG
```

### 4. Application Startup Issues

**Symptoms:**
- Spring Boot application fails to start
- DataSource configuration errors

**Solutions:**
```bash
# Verify infrastructure is running
./start-infrastructure.sh

# Check application.yml configuration
# Ensure MySQL is accessible at localhost:3306

# Run with debug mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# Check for port conflicts
lsof -i :8080
```

### 5. No Customer Data

**Symptoms:**
- Customer table is empty
- DataSeeding not working

**Solutions:**
```bash
# Check if seeding completed
# Look for logs: "Successfully seeded X customer records"

# Manually check customer count
curl http://localhost:8080/api/customers | jq length

# Force re-seeding (delete all customers first)
curl -X DELETE http://localhost:8080/api/customers/bulk  # if implemented
# Or restart application
```

### 6. No CDC Events in Kafka

**Symptoms:**
- No messages in customer-changes topic
- Consumer not receiving messages

**Solutions:**
```bash
# Check if topic exists
docker exec kafka kafka-topics --bootstrap-server localhost:29092 --list

# Create topic manually if missing
docker exec kafka kafka-topics --bootstrap-server localhost:29092 --create --topic customer-changes --partitions 3 --replication-factor 1

# Check messages in topic
docker exec kafka kafka-console-consumer --bootstrap-server localhost:29092 --topic customer-changes --from-beginning

# Verify Debezium is running (check application logs)
```

### 7. Consumer Not Processing Messages

**Symptoms:**
- Messages in Kafka but not in customer_copy table
- Consumer errors in logs

**Solutions:**
```bash
# Check consumer group status
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:29092 --describe --group customer-consumer-group

# Reset consumer offset if needed
docker exec kafka kafka-consumer-groups --bootstrap-server localhost:29092 --reset-offsets --group customer-consumer-group --topic customer-changes --to-earliest --execute

# Check customer_copy table
curl http://localhost:8080/api/customers/copies/count
```

### 8. Performance Issues

**Symptoms:**
- Slow CDC processing
- High resource usage

**Solutions:**
```bash
# Monitor Docker container resources
docker stats

# Increase JVM heap size
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run

# Tune Kafka consumer settings in application.yml
```

## Verification Commands

### Quick Health Check
```bash
# Check all services
docker compose ps

# Test complete flow
./demo-scripts/test-cdc-flow.sh

# Check logs
docker compose logs -f
```

### Manual Testing
```bash
# Create test customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@example.com"}'

# Check if CDC event was created
curl http://localhost:8080/api/customers/copies | jq '.[-1]'
```

## Getting Help

If issues persist:

1. **Check Logs**: Always check application and Docker container logs first
2. **Verify Prerequisites**: Ensure Docker, Java 17, and Maven are properly installed  
3. **Clean Restart**: Stop everything, remove volumes, and restart fresh
4. **Version Compatibility**: Check if your Docker/Java versions are compatible

### Clean Restart Commands
```bash
# Stop everything
docker compose down -v

# Clean Maven
mvn clean

# Remove temporary files
rm -rf /tmp/offsets.dat /tmp/dbhistory.dat

# Start fresh
./start-infrastructure.sh
mvn clean package
mvn spring-boot:run
```