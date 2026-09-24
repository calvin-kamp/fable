package com.fable.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point of the Fable backend.
 * <p>
 * JPA auditing is enabled so that fields annotated with {@code @CreatedDate}
 * and {@code @LastModifiedDate} are populated automatically.
 */
@EnableJpaAuditing
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
