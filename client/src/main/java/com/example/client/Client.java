package com.example.client;

import com.example.api.DateObject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class Client {

	private static final Logger log = LoggerFactory.getLogger(Client.class);

	private static final Instant DATE = Instant.parse("2023-01-10T10:11:12Z");

	public static void main(String[] args) {
		SpringApplication.run(Client.class, args);
	}

	@Bean
	CommandLineRunner run(RestClient.Builder restClientBuilder, @Value("${demo.server-url}") String serverUrl) {
		RestClient restClient = restClientBuilder.baseUrl(serverUrl).build();
		return args -> {
			ZonedDateTime zoned = DATE.atZone(ZoneId.of("Europe/Paris"));
			DateObject sent = new DateObject(DATE, zoned.toOffsetDateTime(), zoned);
			DateObject echoed = restClient.post().uri("/").body(sent).retrieve().body(DateObject.class);
			log.info("sent     = {}", sent);
			log.info("echoed   = {}", echoed);
		};
	}
}
