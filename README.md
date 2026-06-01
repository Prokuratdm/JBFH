# JBFH — Hockey Schools Management API

**JBFH** — REST API для управления детскими хоккейными школами в Беларуси.  
Построен на Spring Boot 4 с JWT-аутентификацией, ролевой моделью доступа и Swagger-документацией.

---

## Технологический стек

- **Java 25**
- **Spring Boot 4.0.6** (WebMvc, Security, Data JPA, Quartz, Actuator)
- **PostgreSQL** (основная БД)
- **JWT** (аутентификация via `jjwt`)
- **SpringDoc OpenAPI 3.0.2** (Swagger UI)
- **Testcontainers** (интеграционные тесты с PostgreSQL)
- **JUnit 5 + Mockito** (юнит-тесты)
- **Gradle** (сборка)

---

## Структура проекта

```
src/
├── main/
│   ├── java/com/par/jbfh/
│   │   ├── JbfhApplication.java
│   │   ├── auth/
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java      # POST /api/v1/auth/login
│   │   │   │   ├── RoleController.java       # GET  /api/v1/roles
│   │   │   │   └── UserController.java       # POST/GET /api/v1/users
│   │   │   ├── dto/
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   └── UserResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Club.java
│   │   │   │   ├── Role.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── ClubRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   └── service/
│   │   │       ├── RoleService.java
│   │   │       └── UserService.java
│   │   ├── common/exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── config/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtService.java
│   │   │   ├── OpenApiConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── UserPrincipal.java
│   │   └── example/controller/
│   │       ├── AdminController.java          # GET /api/v1/admin/**
│   │       └── CoachController.java          # GET /api/v1/coach/**
│   └── resources/
│       └── application.properties
├── test/
│   ├── java/com/par/jbfh/
│   │   ├── AbstractIntegrationTest.java
│   │   ├── JbfhApplicationTests.java
│   │   ├── auth/
│   │   │   ├── controller/RoleControllerTest.java
│   │   │   └── service/RoleServiceTest.java
│   │   └── example/controller/
│   │       ├── AdminControllerTest.java
│   │       └── CoachControllerTest.java
│   └── resources/
│       ├── application.properties
│       └── testcontainers.properties
├── compose.yaml                     # Docker Compose (PostgreSQL)
├── build.gradle
└── settings.gradle
```

---

## API Endpoints

### Публичные (без аутентификации)

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/v1/roles` | Получить список всех ролей |
| `POST` | `/api/v1/auth/login` | Аутентификация, получение JWT токена |
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/api-docs` | OpenAPI JSON спецификация |

### Требуют аутентификации (JWT Bearer Token)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `GET` | `/api/v1/admin/dashboard` | `ROLE_ADMIN` | Админ-панель |
| `GET` | `/api/v1/admin/stats` | `ROLE_ADMIN` | Статистика системы |
| `GET` | `/api/v1/coach/my-team` | `ROLE_COACH`, `ROLE_MAIN_COACH` | Моя команда |
| `GET` | `/api/v1/coach/schedule` | `ROLE_COACH`, `ROLE_MAIN_COACH` | Расписание |
| `POST` | `/api/v1/users` | `ROLE_ADMIN`, `ROLE_CLUB` | Создать пользователя |
| `GET` | `/api/v1/users/{id}` | `ROLE_ADMIN` | Получить пользователя по ID |

---

## Фиксированные роли

При старте приложения автоматически создаются 6 ролей:

- `ROLE_ADMIN`
- `ROLE_CLUB`
- `ROLE_METHODIST`
- `ROLE_CLUB_METHODIST`
- `ROLE_COACH`
- `ROLE_MAIN_COACH`

---

## Swagger UI

После запуска приложения Swagger UI доступен по адресу:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON спецификация:

```
http://localhost:8080/api-docs
```

Swagger UI настроен с JWT Bearer аутентификацией — можно нажать **Authorize** и ввести токен.

---

## Запуск

### 1. Запустить PostgreSQL через Docker

```bash
docker compose up -d
```

### 2. Запустить приложение

```bash
./gradlew bootRun
```

Приложение будет доступно по адресу: `http://localhost:8080`

---

## Тестирование

Тесты используют **Testcontainers** — автоматически запускают PostgreSQL в Docker перед выполнением интеграционных тестов.

```bash
./gradlew test
```

**Важно:** Перед запуском тестов убедитесь, что Docker Desktop запущен, и в настройках Docker Desktop включена опция **Expose daemon on tcp://localhost:2375 without TLS** (Settings → General).

---

## Конфигурация

### База данных (PostgreSQL)

Настройки в `src/main/resources/application.properties`:

| Параметр | Значение |
|----------|----------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/mydatabase` |
| `spring.datasource.username` | `myuser` |
| `spring.datasource.password` | `secret` |

### JWT

| Параметр | Значение |
|----------|----------|
| `jwt.secret` | `jbfh-secret-key-2026-...` |
| `jwt.expiration` | `86400000` (24 часа) |

---

## Логирование

Уровень логирования для пакета `com.par.jbfh` — `DEBUG`.  
Логи выводятся в консоль (stdout).