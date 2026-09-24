package com.example.server;

import com.example.api.DateObject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@RestController
@SpringBootApplication
public class Server {

	private static final Logger log = LoggerFactory.getLogger(Server.class);

	public static void main(String[] args) {
		SpringApplication.run(Server.class, args);
	}

	@GetMapping("/")
	public DateObject get() {
		return new DateObject(Instant.now(), OffsetDateTime.now(), ZonedDateTime.now());
	}

	@PostMapping("/")
	public DateObject post(@RequestBody DateObject dateObject) {
		log.info("date object= {}", dateObject);
		return dateObject;
	}

	@Bean
	public CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setMaxPayloadLength(10000);
		filter.setIncludeHeaders(false);
		filter.setAfterMessagePrefix("REQUEST DATA: ");
		return filter;
	}

}
