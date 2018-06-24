package com.bjohnson.lineserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LineserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(LineserverApplication.class, args);
	}
}
