package com.lethargicdev.cdc.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lethargicdev.cdc.entity.CustomerCopy;
import com.lethargicdev.cdc.repository.CustomerCopyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomerChangeConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CustomerChangeConsumer.class);
    
    @Autowired
    private CustomerCopyRepository customerCopyRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "customer-changes", groupId = "customer-consumer-group")
    public void consume(String message) {
        try {
            logger.info("Received message: {}", message);
            
            JsonNode changeEvent = objectMapper.readTree(message);
            String operation = changeEvent.get("operation").asText();
            
            switch (operation) {
                case "c": // Create
                    handleInsert(changeEvent.get("after"));
                    break;
                case "u": // Update
                    handleUpdate(changeEvent.get("after"));
                    break;
                case "d": // Delete
                    handleDelete(changeEvent.get("before"));
                    break;
                default:
                    logger.warn("Unknown operation: {}", operation);
            }
            
        } catch (Exception e) {
            logger.error("Error processing message: {}", message, e);
        }
    }

    private void handleInsert(JsonNode after) {
        try {
            CustomerCopy customerCopy = createCustomerCopy(after, "INSERT");
            customerCopyRepository.save(customerCopy);
            logger.info("Inserted customer copy with original ID: {}", customerCopy.getOriginalId());
        } catch (Exception e) {
            logger.error("Error handling insert", e);
        }
    }

    private void handleUpdate(JsonNode after) {
        try {
            CustomerCopy customerCopy = createCustomerCopy(after, "UPDATE");
            customerCopyRepository.save(customerCopy);
            logger.info("Updated customer copy with original ID: {}", customerCopy.getOriginalId());
        } catch (Exception e) {
            logger.error("Error handling update", e);
        }
    }

    private void handleDelete(JsonNode before) {
        try {
            CustomerCopy customerCopy = createCustomerCopy(before, "DELETE");
            customerCopyRepository.save(customerCopy);
            logger.info("Marked customer copy as deleted with original ID: {}", customerCopy.getOriginalId());
        } catch (Exception e) {
            logger.error("Error handling delete", e);
        }
    }

    private CustomerCopy createCustomerCopy(JsonNode customerData, String operationType) {
        CustomerCopy customerCopy = new CustomerCopy();
        
        customerCopy.setOriginalId(customerData.get("id").asLong());
        customerCopy.setFirstName(getStringValue(customerData, "first_name"));
        customerCopy.setLastName(getStringValue(customerData, "last_name"));
        customerCopy.setEmail(getStringValue(customerData, "email"));
        customerCopy.setPhone(getStringValue(customerData, "phone"));
        customerCopy.setAddress(getStringValue(customerData, "address"));
        customerCopy.setCity(getStringValue(customerData, "city"));
        customerCopy.setState(getStringValue(customerData, "state"));
        customerCopy.setZipCode(getStringValue(customerData, "zip_code"));
        customerCopy.setOperationType(operationType);
        customerCopy.setProcessedAt(LocalDateTime.now());
        
        // Parse timestamps
        if (customerData.has("created_at") && !customerData.get("created_at").isNull()) {
            customerCopy.setCreatedAt(parseTimestamp(customerData.get("created_at").asLong()));
        }
        if (customerData.has("updated_at") && !customerData.get("updated_at").isNull()) {
            customerCopy.setUpdatedAt(parseTimestamp(customerData.get("updated_at").asLong()));
        }
        
        return customerCopy;
    }

    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }

    private LocalDateTime parseTimestamp(long timestamp) {
        // Debezium sends timestamps as microseconds since epoch
        return LocalDateTime.ofEpochSecond(timestamp / 1_000_000, 
                                         (int) ((timestamp % 1_000_000) * 1000), 
                                         java.time.ZoneOffset.UTC);
    }
}