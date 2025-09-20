package com.lethargicdev.cdc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CdcKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdcKafkaApplication.class, args);
    }
}