#!/bin/bash

echo "=== Starting CDC Kafka Integration Infrastructure ==="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "🐳 Starting infrastructure with Docker Compose..."
docker compose up -d

echo "⏳ Waiting for services to be healthy..."
sleep 10

# Check if MySQL is ready
echo "🔍 Checking MySQL connection..."
for i in {1..30}; do
    if docker exec mysql mysql -u root -ppassword -e "SELECT 1" > /dev/null 2>&1; then
        echo "✅ MySQL is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ MySQL failed to start properly"
        exit 1
    fi
    sleep 2
done

# Check if Kafka is ready
echo "🔍 Checking Kafka connection..."
for i in {1..30}; do
    if docker exec kafka kafka-topics --bootstrap-server localhost:29092 --list > /dev/null 2>&1; then
        echo "✅ Kafka is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Kafka failed to start properly"
        exit 1
    fi
    sleep 2
done

# Setup database
echo "🗄️ Setting up database schema..."
docker exec -i mysql mysql -u root -ppassword customer_db < sql/setup.sql

# Create Kafka topic
echo "📡 Creating Kafka topic..."
docker exec kafka kafka-topics --bootstrap-server localhost:29092 --create --topic customer-changes --partitions 3 --replication-factor 1 --if-not-exists

echo ""
echo "🎉 Infrastructure is ready!"
echo ""
echo "Services available at:"
echo "  📊 MySQL: localhost:3306 (root/password)"
echo "  🚀 Kafka: localhost:9092"
echo "  🌐 Kafka UI: http://localhost:8090"
echo "  💾 Adminer (DB UI): http://localhost:8091"
echo ""
echo "Next steps:"
echo "  1. Build the application: mvn clean package"
echo "  2. Run the application: mvn spring-boot:run"
echo "  3. Test the CDC flow: ./demo-scripts/test-cdc-flow.sh"