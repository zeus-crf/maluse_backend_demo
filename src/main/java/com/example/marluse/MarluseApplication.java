package com.example.marluse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarluseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarluseApplication.class, args);
	}

}
