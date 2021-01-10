package com.linkshorter.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LinkShorterApiApplication {

	private static final Logger log = LoggerFactory.getLogger(LinkShorterApiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LinkShorterApiApplication.class, args);
	}

}
