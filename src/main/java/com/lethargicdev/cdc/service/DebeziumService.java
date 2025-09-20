package com.lethargicdev.cdc.service;

import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class DebeziumService {

    private static final Logger logger = LoggerFactory.getLogger(DebeziumService.class);
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void start() {
        final Configuration config = Configuration.create()
                .with("name", "customer-mysql-connector")
                .with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", "/tmp/offsets.dat")
                .with("database.hostname", "localhost")
                .with("database.port", "3306")
                .with("database.user", "debezium")
                .with("database.password", "debezium")
                .with("database.server.id", "85744")
                .with("database.server.name", "customer-mysql-db")
                .with("database.include.list", "customer_db")
                .with("table.include.list", "customer_db.customer")
                .with("include.schema.changes", "false")
                .with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename", "/tmp/dbhistory.dat")
                .build();

        debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(config.asProperties())
                .notifying(this::handleChangeEvent)
                .build();

        executor.execute(debeziumEngine);
        logger.info("Debezium engine started successfully");
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        logger.info("Key = '{}' value = '{}'", sourceRecord.key(), sourceRecord.value());

        Struct sourceRecordChangeValue = (Struct) sourceRecord.value();
        if (sourceRecordChangeValue != null) {
            String operation = sourceRecordChangeValue.getString("op");
            String message = createChangeMessage(sourceRecordChangeValue, operation);
            
            // Send to Kafka topic
            kafkaTemplate.send("customer-changes", message);
            logger.info("Sent change event to Kafka: {}", message);
        }
    }

    private String createChangeMessage(Struct sourceRecordChangeValue, String operation) {
        StringBuilder message = new StringBuilder();
        message.append("{");
        message.append("\"operation\":\"").append(operation).append("\",");
        
        if ("d".equals(operation)) {
            // Delete operation - get data from "before"
            Struct before = (Struct) sourceRecordChangeValue.get("before");
            message.append("\"before\":").append(structToJson(before));
        } else if ("c".equals(operation)) {
            // Create operation - get data from "after"
            Struct after = (Struct) sourceRecordChangeValue.get("after");
            message.append("\"after\":").append(structToJson(after));
        } else if ("u".equals(operation)) {
            // Update operation - get both before and after
            Struct before = (Struct) sourceRecordChangeValue.get("before");
            Struct after = (Struct) sourceRecordChangeValue.get("after");
            message.append("\"before\":").append(structToJson(before)).append(",");
            message.append("\"after\":").append(structToJson(after));
        }
        
        message.append("}");
        return message.toString();
    }

    private String structToJson(Struct struct) {
        if (struct == null) return "null";
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Field field : struct.schema().fields()) {
            if (!first) json.append(",");
            json.append("\"").append(field.name()).append("\":");
            
            Object value = struct.get(field);
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }

    @PreDestroy
    public void stop() throws IOException {
        if (debeziumEngine != null) {
            debeziumEngine.close();
            logger.info("Debezium engine stopped");
        }
    }
}