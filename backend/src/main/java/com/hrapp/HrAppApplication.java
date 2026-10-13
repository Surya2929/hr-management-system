package com.hrapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class HrAppApplication {
    public static void main(String[] args) {
        // Server runs in UTC; use IST so "today" for attendance and leave matches the users' day
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(HrAppApplication.class, args);
    }
}
