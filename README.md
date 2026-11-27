English | [Español](README.es.md)

# LogLens

[![CI](https://github.com/ManuelJesusPascual/LogLens/actions/workflows/ci.yml/badge.svg)](https://github.com/ManuelJesusPascual/LogLens/actions/workflows/ci.yml)

REST API that takes a plain-text application log and returns its unique errors, how many times each one appears and on which lines.

## Why

Finding what actually failed in a long log usually means scrolling past the same stack trace hundreds of times. LogLens answers "which different errors are in here?" with a single HTTP call, so it can be used from a terminal or a CI job without setting up an observability platform.

## Features

- Detects lines containing `ERROR`, `Exception` or `Caused by`
- Groups identical errors, ignoring whatever precedes the marker (timestamps, for example), and lists them in order of first appearance
- API key authentication, with a monthly quota of 500 requests per key
- The quota is enforced by a single conditional `UPDATE`, so concurrent requests cannot overrun it
- API keys are stored only as SHA-256 hashes
- Errors returned as [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details, without internal messages
- OpenAPI documentation with Swagger UI

## Stack

Java 21, Spring Boot 4.2, Spring JDBC, SQLite, springdoc-openapi, Lombok, Maven, JUnit 5, Docker and GitHub Actions.

## Getting started

You need Docker.

```bash
git clone https://github.com/ManuelJesusPascual/LogLens.git
cd LogLens
cp .env.example .env        # then set ADMIN_PASSWORD in .env
docker compose up --build
```

The API listens on `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui/index.html`.

To run it without Docker you need JDK 21:

```bash
ADMIN_PASSWORD=choose-one ./mvnw spring-boot:run
```

In PowerShell:

```powershell
$env:ADMIN_PASSWORD = "choose-one"; ./mvnw.cmd spring-boot:run
```

The application does not start without `ADMIN_PASSWORD`.

## Usage

The examples use a POSIX shell.

### 1. Create an API key

```bash
curl -X POST http://localhost:8080/admin/generate-key -H "X-Admin-Password: choose-one"
```

The response body is the key. It is shown only once.

### 2. Analyze a log

```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Authorization: Bearer YOUR_KEY" \
  -H "Content-Type: text/plain" \
  --data-binary @app.log
```

For this `app.log`:

```
2025-12-27 08:00:01 INFO Starting application
2025-12-27 08:00:02 ERROR NullPointerException at UserService.java:45
2025-12-27 08:00:04 ERROR Connection timeout
2025-12-27 08:00:06 ERROR Connection timeout
```

the response is:

```json
{
  "errors": [
    {
      "message": "ERROR NullPointerException at UserService.java:45",
      "count": 1,
      "lines": [2]
    },
    {
      "message": "ERROR Connection timeout",
      "count": 2,
      "lines": [3, 4]
    }
  ],
  "summary": {
    "totalLines": 4,
    "errorCount": 2
  }
}
```

`errorCount` is the number of unique errors.

### Errors

```json
{
  "detail": "Missing API Key",
  "status": 401,
  "title": "Unauthorized"
}
```

| Status | When |
|--------|------|
| 400 | Empty log |
| 401 | Missing or unknown API key |
| 403 | Wrong admin password |
| 413 | Log larger than 1 MB |
| 415 | Content type other than `text/plain` |
| 429 | Monthly quota used up |

## Tests

```bash
./mvnw verify
```

They cover log parsing, the quota under concurrency, the monthly reset, key hashing, authentication and HTTP error statuses.

## Known limitations

- Error detection is plain substring matching: an `INFO` line that mentions `Exception` is counted too.
- Every request with a valid key counts against the quota, including rejected ones such as an empty log.
- There is no endpoint to list or revoke keys.
- The admin endpoint has no rate limiting. Do not expose it to the internet as it is.
- SQLite limits the service to a single instance.
- The application serves plain HTTP; put it behind a TLS proxy.

## License

[MIT](LICENSE)
