package com.princz_mia.viaual04_gourmetgo_backend;

import org.springframework.boot.SpringApplication;

public class TestViaual04GourmetgoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(Viaual04GourmetgoBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
