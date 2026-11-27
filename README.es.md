[English](README.md) | Español

# LogLens

[![CI](https://github.com/ManuelJesusPascual/LogLens/actions/workflows/ci.yml/badge.svg)](https://github.com/ManuelJesusPascual/LogLens/actions/workflows/ci.yml)

API REST que recibe el log de una aplicación en texto plano y devuelve sus errores únicos, cuántas veces aparece cada uno y en qué líneas.

## Por qué

Encontrar qué ha fallado de verdad en un log largo suele obligar a pasar cientos de veces por la misma traza. LogLens responde a "¿qué errores distintos hay aquí?" con una sola llamada HTTP, así que se puede usar desde una terminal o un job de CI sin montar una plataforma de observabilidad.

## Funcionalidades

- Detecta las líneas que contienen `ERROR`, `Exception` o `Caused by`
- Agrupa los errores idénticos, ignorando lo que precede al marcador (la fecha y hora, por ejemplo), y los lista en orden de primera aparición
- Autenticación por API key, con una cuota mensual de 500 peticiones por key
- La cuota se aplica con un único `UPDATE` condicional, así que las peticiones concurrentes no pueden superarla
- Las API keys se guardan solo como hash SHA-256
- Errores en formato problem details de [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), sin mensajes internos
- Documentación OpenAPI con Swagger UI

## Stack

Java 21, Spring Boot 4.2, Spring JDBC, SQLite, springdoc-openapi, Lombok, Maven, JUnit 5, Docker y GitHub Actions.

## Arranque

Necesitas Docker.

```bash
git clone https://github.com/ManuelJesusPascual/LogLens.git
cd LogLens
cp .env.example .env        # después define ADMIN_PASSWORD en .env
docker compose up --build
```

La API escucha en `http://localhost:8080`. Swagger UI está en `http://localhost:8080/swagger-ui/index.html`.

Para arrancarla sin Docker necesitas JDK 21:

```bash
ADMIN_PASSWORD=elige-una ./mvnw spring-boot:run
```

En PowerShell:

```powershell
$env:ADMIN_PASSWORD = "elige-una"; ./mvnw.cmd spring-boot:run
```

La aplicación no arranca sin `ADMIN_PASSWORD`.

## Uso

Los ejemplos usan una shell POSIX.

### 1. Crear una API key

```bash
curl -X POST http://localhost:8080/admin/generate-key -H "X-Admin-Password: elige-una"
```

El cuerpo de la respuesta es la key. Solo se muestra una vez.

### 2. Analizar un log

```bash
curl -X POST http://localhost:8080/api/analyze \
  -H "Authorization: Bearer TU_KEY" \
  -H "Content-Type: text/plain" \
  --data-binary @app.log
```

Para este `app.log`:

```
2025-12-27 08:00:01 INFO Starting application
2025-12-27 08:00:02 ERROR NullPointerException at UserService.java:45
2025-12-27 08:00:04 ERROR Connection timeout
2025-12-27 08:00:06 ERROR Connection timeout
```

la respuesta es:

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

`errorCount` es el número de errores únicos.

### Errores

```json
{
  "detail": "Missing API Key",
  "status": 401,
  "title": "Unauthorized"
}
```

| Estado | Cuándo |
|--------|--------|
| 400 | Log vacío |
| 401 | Falta la API key o no existe |
| 403 | Contraseña de administrador incorrecta |
| 413 | Log de más de 1 MB |
| 415 | Tipo de contenido distinto de `text/plain` |
| 429 | Cuota mensual agotada |

## Tests

```bash
./mvnw verify
```

Cubren el análisis de logs, la cuota con concurrencia, el reinicio mensual, el hash de las keys, la autenticación y los códigos de error HTTP.

## Limitaciones conocidas

- La detección de errores es una simple búsqueda de texto: una línea `INFO` que mencione `Exception` también cuenta.
- Toda petición con una key válida consume cuota, incluidas las rechazadas, como un log vacío.
- No hay endpoint para listar ni revocar keys.
- El endpoint de administración no limita la frecuencia de peticiones. No lo expongas a internet tal como está.
- SQLite limita el servicio a una sola instancia.
- La aplicación sirve HTTP sin cifrar; ponla detrás de un proxy TLS.

## Licencia

[MIT](LICENSE)
