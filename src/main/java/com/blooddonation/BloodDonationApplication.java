package com.blooddonation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.blooddonation.model")
@EnableJpaRepositories("com.blooddonation.repository")
public class BloodDonationApplication {

	public static void main(String[] args) {
		SpringApplication.run(BloodDonationApplication.class, args);
	}

}
