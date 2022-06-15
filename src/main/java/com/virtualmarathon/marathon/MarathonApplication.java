package com.virtualmarathon.marathon;

import com.virtualmarathon.marathon.repository.MarathonRepository;
import com.virtualmarathon.marathon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
public class MarathonApplication {

	@Autowired
	UserRepository userRepository;

	@Autowired
	MarathonRepository marathonRepository;

	public static void main(String[] args) {
		SpringApplication.run(MarathonApplication.class, args);
	}
}
