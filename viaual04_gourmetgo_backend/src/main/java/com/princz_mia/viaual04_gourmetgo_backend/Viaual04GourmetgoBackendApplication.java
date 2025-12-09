package com.princz_mia.viaual04_gourmetgo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Viaual04GourmetgoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(Viaual04GourmetgoBackendApplication.class, args);
	}

}
