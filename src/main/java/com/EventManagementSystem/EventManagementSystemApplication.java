package com.EventManagementSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.EventManagementSystem.service.EventService;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EventManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementSystemApplication.class, args);
    }

    @Autowired
    private EventService eventService;

    @Bean
    public CommandLineRunner syncEventStatusesOnStartup() {
        return args -> eventService.syncEventStatuses();
    }
}



