package com.lethargicdev.cdc.service;

import com.lethargicdev.cdc.entity.Customer;
import com.lethargicdev.cdc.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataSeedingService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeedingService.class);
    
    @Autowired
    private CustomerRepository customerRepository;
    
    private final String[] firstNames = {
        "John", "Jane", "Michael", "Sarah", "David", "Lisa", "Robert", "Emily", "James", "Jessica",
        "William", "Ashley", "Richard", "Amanda", "Charles", "Melissa", "Joseph", "Kimberly", "Thomas", "Donna",
        "Christopher", "Margaret", "Daniel", "Susan", "Matthew", "Dorothy", "Anthony", "Lisa", "Mark", "Nancy",
        "Donald", "Karen", "Steven", "Betty", "Paul", "Helen", "Andrew", "Sandra", "Joshua", "Deborah",
        "Kenneth", "Carol", "Kevin", "Ruth", "Brian", "Sharon", "George", "Michelle", "Edward", "Laura"
    };
    
    private final String[] lastNames = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
        "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
        "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts"
    };
    
    private final String[] cities = {
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego",
        "Dallas", "San Jose", "Austin", "Jacksonville", "Fort Worth", "Columbus", "Charlotte", "San Francisco",
        "Indianapolis", "Seattle", "Denver", "Washington", "Boston", "El Paso", "Nashville", "Detroit", "Portland"
    };
    
    private final String[] states = {
        "NY", "CA", "IL", "TX", "AZ", "PA", "TX", "CA", "TX", "CA", "TX", "FL", "TX", "OH", "NC", "CA",
        "IN", "WA", "CO", "DC", "MA", "TX", "TN", "MI", "OR"
    };

    @Override
    public void run(String... args) {
        long count = customerRepository.count();
        if (count < 10000) {
            logger.info("Seeding database with {} customer records", 10000 - count);
            seedCustomers((int)(10000 - count));
        } else {
            logger.info("Database already contains {} customer records", count);
        }
    }

    private void seedCustomers(int numberOfCustomers) {
        Random random = new Random();
        List<Customer> customers = new ArrayList<>();
        
        for (int i = 0; i < numberOfCustomers; i++) {
            Customer customer = new Customer();
            
            customer.setFirstName(firstNames[random.nextInt(firstNames.length)]);
            customer.setLastName(lastNames[random.nextInt(lastNames.length)]);
            customer.setEmail(generateEmail(customer.getFirstName(), customer.getLastName(), i));
            customer.setPhone(generatePhone(random));
            customer.setAddress(generateAddress(random));
            
            int cityIndex = random.nextInt(cities.length);
            customer.setCity(cities[cityIndex]);
            customer.setState(states[cityIndex]);
            customer.setZipCode(generateZipCode(random));
            customer.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            
            customers.add(customer);
            
            // Save in batches of 1000
            if (customers.size() == 1000) {
                customerRepository.saveAll(customers);
                customers.clear();
                logger.debug("Saved batch of 1000 customers");
            }
        }
        
        // Save remaining customers
        if (!customers.isEmpty()) {
            customerRepository.saveAll(customers);
        }
        
        logger.info("Successfully seeded {} customer records", numberOfCustomers);
    }
    
    private String generateEmail(String firstName, String lastName, int index) {
        return firstName.toLowerCase() + "." + lastName.toLowerCase() + index + "@example.com";
    }
    
    private String generatePhone(Random random) {
        return String.format("(%03d) %03d-%04d", 
                            200 + random.nextInt(800), 
                            100 + random.nextInt(900), 
                            1000 + random.nextInt(9000));
    }
    
    private String generateAddress(Random random) {
        int streetNumber = 100 + random.nextInt(9900);
        String[] streetNames = {"Main St", "Oak Ave", "Park Blvd", "First St", "Second Ave", "Elm St", "Maple Ave", "Pine St"};
        return streetNumber + " " + streetNames[random.nextInt(streetNames.length)];
    }
    
    private String generateZipCode(Random random) {
        return String.format("%05d", 10000 + random.nextInt(90000));
    }
}