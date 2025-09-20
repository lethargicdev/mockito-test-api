-- Create database if not exists
CREATE DATABASE IF NOT EXISTS customer_db;
USE customer_db;

-- Create customer table
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(10),
    zip_code VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Create customer_copy table
CREATE TABLE IF NOT EXISTS customer_copy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(10),
    zip_code VARCHAR(10),
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    operation_type VARCHAR(10) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_customer_copy_original_id ON customer_copy(original_id);
CREATE INDEX idx_customer_copy_operation ON customer_copy(operation_type);

-- Grant privileges to debezium user
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
GRANT ALL PRIVILEGES ON customer_db.* TO 'debezium'@'%';
FLUSH PRIVILEGES;