package com.example.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.api.DateObject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerTest {

	@LocalServerPort
	int port;

	@Test
	void zonedDateTimeTravelsAsOffsetOnlyAndArrivesNormalizedToUtc() {
		RestTestClient restTestClient = RestTestClient.bindToServer()
				.baseUrl("http://localhost:" + port)
				.build();

		ZonedDateTime zoned = Instant.parse("2023-01-10T10:11:12Z").atZone(ZoneId.of("Europe/Paris"));
		DateObject sent = new DateObject(Instant.parse("2023-01-10T10:11:12Z"), zoned.toOffsetDateTime(),
				zoned);

		restTestClient.post().uri("/")
				.contentType(MediaType.APPLICATION_JSON)
				.body(sent)
				.exchange()
				.expectStatus().isOk();

		String json = restTestClient.get().uri("/")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult()
				.getResponseBody();

		// on the wire, both offset and zoned carry a numeric offset, never a zone id
		assertThat(json).containsPattern("\"offset\":\"[^\"]*(Z|[+-]\\d{2}:\\d{2})\"");
		assertThat(json).containsPattern("\"zoned\":\"[^\"]*(Z|[+-]\\d{2}:\\d{2})\"");
		assertThat(json).doesNotContain("[");

		DateObject received = restTestClient.get().uri("/")
				.exchange()
				.expectStatus().isOk()
				.expectBody(DateObject.class)
				.returnResult()
				.getResponseBody();

		// after deserialization, the offsets are normalized to UTC
		assertThat(received.offset().getOffset()).isEqualTo(ZoneOffset.UTC);
		assertThat(received.zoned().getZone()).isEqualTo(ZoneOffset.UTC);
	}
}
