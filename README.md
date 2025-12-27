# LogLens API

LogLens is a lightweight developer tool built with Java 21 and Spring Boot 3. It analyzes application logs and provides
a concise summary of unique errors, including their occurrences and the lines where they appear. Designed for fast
deployment and easy integration into CI pipelines, LogLens helps developers quickly identify and debug issues without
complex observability setups.

## 🚀 Features

- Analyze plain-text logs via a simple API
- Detect `ERROR`, `Exception`, and `Caused by` entries
- Count occurrences of each unique error
- Track the lines where each error appears
- Returns a clean JSON response
- Quick deployment with Spring Boot

## 📦 Installation

1. Clone the repository:

```bash
git clone https://github.com/your-username/loglens.git
cd loglens
```

2. Build the project with Maven:

```bash
mvn clean package
```

3. Run the application:

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/analyze`

## 🔹 Usage

### Endpoint

```
POST /analyze
Content-Type: text/plain
```

### Request body

Paste your log as plain text. Example:

```
2025-12-27 08:00:01 INFO Starting application
2025-12-27 08:00:02 ERROR NullPointerException at UserService.java:45
2025-12-27 08:00:04 ERROR Connection timeout
```

### Response

```json
{
  "errors": [
    {
      "message": "ERROR NullPointerException at UserService.java:45",
      "count": 1,
      "lines": [
        2
      ]
    },
    {
      "message": "ERROR Connection timeout",
      "count": 1,
      "lines": [
        4
      ]
    }
  ],
  "summary": {
    "totalLines": 4,
    "errorCount": 2
  }
}
```

## ⚡ Error Handling

- Sending an empty log returns a `400 Bad Request` with message: `"Empty log"`

## 🛠️ Technologies

- Java 21
- Spring Boot 3
- Lombok
- Maven
- OpenCSV (for future parsing enhancements)
- springdoc-openapi (Swagger UI)

## 📊 Swagger / OpenAPI

After running the app, you can access the API docs at:

```
http://localhost:8080/swagger-ui.html
```

## 🧪 Testing

Run unit tests:

```bash
mvn test
```

Tests cover:

- Basic log parsing
- Error detection (`ERROR`, `Exception`, `Caused by`)
- Grouping repeated errors
- Handling logs without errors