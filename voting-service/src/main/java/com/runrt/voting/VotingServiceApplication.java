package com.runrt.voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;
import java.time.ZoneId;

@SpringBootApplication
public class VotingServiceApplication {
    public static void main(String[] args) {
        // DEBUG: Check what timezone the JVM actually starts with
        System.out.println("=== VOTING SERVICE TIMEZONE DEBUG ===");
        System.out.println("Default JVM TimeZone: " + TimeZone.getDefault().getID());
        System.out.println("System property user.timezone: " + System.getProperty("user.timezone"));
        System.out.println("ZoneId systemDefault: " + ZoneId.systemDefault().getId());
        System.out.println("====================================");

        SpringApplication.run(VotingServiceApplication.class, args);
    }
}
