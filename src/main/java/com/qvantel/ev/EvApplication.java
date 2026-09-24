package com.qvantel.ev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.qvantel")
public class EvApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvApplication.class, args);
	}

}
