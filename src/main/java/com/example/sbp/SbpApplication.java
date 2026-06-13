package com.example.sbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {JtaAutoConfiguration.class})
@EnableAspectJAutoProxy
public class SbpApplication {
	public static void main(String[] args) {
		SpringApplication.run(SbpApplication.class, args);
	}
}