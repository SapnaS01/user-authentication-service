package com.self.userauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.self.userauth.config.JwtProperties;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class UserauthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserauthApplication.class, args);
		System.out.println("" + UserauthApplication.class.getSimpleName() + " started successfully!");
	}

	@Autowired
	private JwtProperties jwtProperties;

//	to check the JWT properties after the application starts
	@PostConstruct
	public void checkJwtProps() {
	    System.out.println("Access Expiration: " + jwtProperties.getAccessExpiration());
	    System.out.println("Refresh Expiration: " + jwtProperties.getRefreshExpiration());
	}

}
