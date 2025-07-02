package com.onified.distribute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistributeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributeApplication.class, args);
	}

}
