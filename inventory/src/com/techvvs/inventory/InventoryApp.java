package com.techvvs.inventory;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableBatchProcessing
public class InventoryApp {

	public static void main(String[] args) {
		SpringApplication.run(InventoryApp.class, args);
	}

}
