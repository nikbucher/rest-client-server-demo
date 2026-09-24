package com.example.api;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public record DateObject(Instant instant, OffsetDateTime offset, ZonedDateTime zoned) {
}
