package com.lethargicdev.cdc;

import com.lethargicdev.cdc.service.DebeziumService;
import com.lethargicdev.cdc.service.DataSeedingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.kafka.bootstrap-servers=localhost:19092"
})
class CdcKafkaApplicationTests {

    @MockBean
    private DebeziumService debeziumService;
    
    @MockBean
    private DataSeedingService dataSeedingService;

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }
}