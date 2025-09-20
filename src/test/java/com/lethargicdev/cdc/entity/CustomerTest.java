package com.lethargicdev.cdc.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class CustomerTest {

    @Test
    void testCustomerCreation() {
        Customer customer = new Customer(
            "John", "Doe", "john.doe@example.com", 
            "(555) 123-4567", "123 Main St", 
            "New York", "NY", "10001"
        );

        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("(555) 123-4567", customer.getPhone());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals("New York", customer.getCity());
        assertEquals("NY", customer.getState());
        assertEquals("10001", customer.getZipCode());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    void testPreUpdate() {
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail("jane.smith@example.com");
        
        assertNull(customer.getUpdatedAt());
        
        customer.preUpdate();
        
        assertNotNull(customer.getUpdatedAt());
    }
}