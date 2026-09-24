# rest-client-server-demo

[![build](https://github.com/nikbucher/rest-client-server-demo/actions/workflows/build.yml/badge.svg)](https://github.com/nikbucher/rest-client-server-demo/actions/workflows/build.yml)

A minimal demo of how Java's date and time types travel over REST between two Spring Boot
applications: the client sends an `Instant`, an `OffsetDateTime` and a `ZonedDateTime` to the
server, the server logs the raw JSON payload next to the deserialized object, and the client
prints what it gets back.

## What you can observe

With Spring Boot 4 / Jackson 3 defaults:

| Type | Serialized to JSON as | After deserialization |
|---|---|---|
| `Instant` | `2023-01-10T10:11:12Z` | unchanged |
| `OffsetDateTime` | `2023-01-10T11:11:12+01:00` | normalized to UTC (`2023-01-10T10:11:12Z`) |
| `ZonedDateTime` | `2023-01-10T11:11:12+01:00` — same as `OffsetDateTime`, the zone id `[Europe/Paris]` is **not** serialized | normalized to UTC, the original zone is gone |

In other words: the *instants* survive the round trip, but all zone and offset information does not.

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
Client : received = DateObject[instant=2026-09-24T21:18:27.779775413Z, offset=2026-09-24T21:18:27.779778830Z, zoned=2026-09-24T21:18:27.779783163Z]
```

The server log shows the raw JSON the client actually sent and what the server made of it:

```console
Server: REQUEST DATA: POST /, payload={"instant":"2023-01-10T10:11:12Z","offset":"2023-01-10T11:11:12+01:00","zoned":"2023-01-10T11:11:12+01:00"}
Server : date object= DateObject[instant=2023-01-10T10:11:12Z, offset=2023-01-10T10:11:12Z, zoned=2023-01-10T10:11:12Z]
```

Or poke the server directly:

```console
$ curl localhost:9999/
{"instant":"2026-09-24T21:17:56.084987232Z","offset":"2026-09-24T23:17:56.084992065+02:00","zoned":"2026-09-24T23:17:56.084997315+02:00"}
```

## Structure

- `api` — the `DateObject` record shared by client and server
- `server` — REST endpoints plus a request logging filter that logs the raw JSON payload
- `client` — sends and fetches a `DateObject` via Spring's `RestClient`

## License

[MIT](LICENSE)
