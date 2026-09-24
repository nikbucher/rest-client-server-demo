package com.example.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.DateObject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerTest {

	@LocalServerPort
	int port;

	@Autowired
	JsonMapper jsonMapper;

	@Test
	void zonedDateTimeIsSerializedWithOffsetOnlyAndEchoArrivesNormalizedToUtc() {
		RestTestClient restTestClient = RestTestClient.bindToServer()
				.baseUrl("http://localhost:" + port)
				.build();

		ZonedDateTime zoned = Instant.parse("2023-01-10T10:11:12Z").atZone(ZoneId.of("Europe/Paris"));
		DateObject sent = new DateObject(Instant.parse("2023-01-10T10:11:12Z"), zoned.toOffsetDateTime(),
				zoned);

		// Jackson writes ZonedDateTime with a numeric offset only, the Europe/Paris zone id is not serialized
		assertThat(jsonMapper.writeValueAsString(sent))
				.contains("\"zoned\":\"2023-01-10T11:11:12+01:00\"");

		DateObject echoed = restTestClient.post().uri("/")
				.contentType(MediaType.APPLICATION_JSON)
				.body(sent)
				.exchange()
				.expectStatus().isOk()
				.expectBody(DateObject.class)
				.returnResult()
				.getResponseBody();

		// the echoed object has the same instants, but offset and zone are normalized to UTC
		assertThat(echoed.instant()).isEqualTo(sent.instant());
		assertThat(echoed.offset().getOffset()).isEqualTo(ZoneOffset.UTC);
		assertThat(echoed.offset().toInstant()).isEqualTo(sent.instant());
		assertThat(echoed.zoned().getZone()).isEqualTo(ZoneOffset.UTC);
		assertThat(echoed.zoned().toInstant()).isEqualTo(sent.instant());
	}
}
