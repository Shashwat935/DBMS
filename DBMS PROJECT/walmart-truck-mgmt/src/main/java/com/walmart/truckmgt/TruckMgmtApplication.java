package com.walmart.truckmgt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Walmart Warehouse Truck Management System.
 * EnableScheduling turns on the auto-complete simulation job.
 */
@SpringBootApplication
@EnableScheduling
public class TruckMgmtApplication {

    public static void main(String[] args) {
        SpringApplication.run(TruckMgmtApplication.class, args);
    }
}
