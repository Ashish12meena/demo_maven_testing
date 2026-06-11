package com.grras.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Main entry point for Spring Boot application
 *
 * @SpringBootApplication combines:
 *   - @Configuration
 *   - @EnableAutoConfiguration
 *   - @ComponentScan
 */
@SpringBootApplication
public class TestingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestingApplication.class, args);
	}

}
