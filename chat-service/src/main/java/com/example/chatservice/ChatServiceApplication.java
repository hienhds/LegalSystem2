package com.example.chatservice;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
@SpringBootApplication
@EnableJpaRepositories(
		basePackages = "com.example.chatservice.jpa.repository"
)
@EnableMongoRepositories(
		basePackages = "com.example.chatservice.mongo.repository"
)
@EntityScan("com.example.chatservice.jpa.entity")
@EnableFeignClients
public class ChatServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatServiceApplication.class, args);
	}

}
