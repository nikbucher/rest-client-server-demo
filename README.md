# rest-client-server-demo

[![build](https://github.com/nikbucher/rest-client-server-demo/actions/workflows/build.yml/badge.svg)](https://github.com/nikbucher/rest-client-server-demo/actions/workflows/build.yml)

A minimal demo of how Java's date and time types travel over REST between two Spring Boot
applications: the client sends an `Instant`, an `OffsetDateTime` and a `ZonedDateTime` to the
server, the server echoes the deserialized object back, and the client prints both next to
each other. A request logging filter shows the raw JSON in the server log.

## What you can observe

With Spring Boot 4 / Jackson 3 defaults, sending `2023-01-10T10:11:12Z` as `Europe/Paris`:

| Type | On the wire | After deserialization |
|---|---|---|
| `Instant` | `2023-01-10T10:11:12Z` | unchanged |
| `OffsetDateTime` | `2023-01-10T11:11:12+01:00` — the offset **is** transmitted | normalized to UTC: `2023-01-10T10:11:12Z` |
| `ZonedDateTime` | `2023-01-10T11:11:12+01:00` — offset only, the zone id `[Europe/Paris]` is not written | normalized to UTC: `2023-01-10T10:11:12Z`, the original zone is gone |

So the instants survive the round trip, but the offset and zone information is lost — on the
deserializing side, not on the wire. Two Jackson 3 `DateTimeFeature`s are responsible:

- `WRITE_DATES_WITH_ZONE_ID` (disabled by default): `ZonedDateTime` is written with a numeric
  offset only, without the `[Europe/Paris]` zone id.
- `ADJUST_DATES_TO_CONTEXT_TIME_ZONE` (enabled by default): on deserialization, values are
  adjusted to the mapper's context time zone, which defaults to UTC.

## Requirements

- JDK 25

## Run it

Start the server in one terminal:

```console
$ ./gradlew :server:bootRun
```

Run the client in a second terminal:

```console
$ ./gradlew :client:bootRun
...
Client : sent     = DateObject[instant=2023-01-10T10:11:12Z, offset=2023-01-10T11:11:12+01:00, zoned=2023-01-10T11:11:12+01:00[Europe/Paris]]
Client : echoed   = DateObject[instant=2023-01-10T10:11:12Z, offset=2023-01-10T10:11:12Z, zoned=2023-01-10T10:11:12Z]
```

The server log shows the raw JSON the client actually sent and what the server made of it:

```console
Server: REQUEST DATA: POST /, payload={"instant":"2023-01-10T10:11:12Z","offset":"2023-01-10T11:11:12+01:00","zoned":"2023-01-10T11:11:12+01:00"}
Server : date object= DateObject[instant=2023-01-10T10:11:12Z, offset=2023-01-10T10:11:12Z, zoned=2023-01-10T10:11:12Z]
```

Or poke the server directly:

```console
$ curl localhost:9999/
{"instant":"2026-09-24T21:35:21.672722397Z","offset":"2026-09-24T23:35:21.672725689+02:00","zoned":"2026-09-24T23:35:21.672730439+02:00"}
```

## Keeping offset and zone

Both features can be toggled via `spring.jackson.datatype.datetime.*` properties. Pass them to
both sides:

```console
$ ./gradlew :server:bootRun --args='--spring.jackson.datatype.datetime.write-dates-with-zone-id=true --spring.jackson.datatype.datetime.adjust-dates-to-context-time-zone=false'
```

```console
$ ./gradlew :client:bootRun --args='--spring.jackson.datatype.datetime.write-dates-with-zone-id=true --spring.jackson.datatype.datetime.adjust-dates-to-context-time-zone=false'
...
Client : sent     = DateObject[instant=2023-01-10T10:11:12Z, offset=2023-01-10T11:11:12+01:00, zoned=2023-01-10T11:11:12+01:00[Europe/Paris]]
Client : echoed   = DateObject[instant=2023-01-10T10:11:12Z, offset=2023-01-10T11:11:12+01:00, zoned=2023-01-10T11:11:12+01:00[Europe/Paris]]
```

Now the zone id is written (`"zoned":"2023-01-10T11:11:12+01:00[Europe/Paris]"`) and nothing is
normalized on the way back — offset and zone survive the round trip.

## Structure

- `api` — the `DateObject` record shared by client and server
- `server` — REST endpoints plus a request logging filter that logs the raw JSON payload
- `client` — sends a `DateObject` via Spring's `RestClient` and compares it with the echo

## License

[MIT](LICENSE)
