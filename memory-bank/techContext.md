# Tech Context: JBFH

## Technology Stack
- **Java 25**
- **Spring Boot 4.0.6** (WebMvc, Security, Data JPA, Quartz, Actuator)
- **PostgreSQL** (основная БД, запускается через Docker Compose)
- **JWT** (jjwt library, HS256, 24h expiration)
- **SpringDoc OpenAPI 3.0.2** (Swagger UI + OpenAPI JSON)
- **Testcontainers** (интеграционные тесты с PostgreSQL в Docker)
- **JUnit 5 + Mockito** (юнит-тесты)
- **Gradle** (сборка, Groovy DSL)
- **Lombok** (@Getter, @Setter, @RequiredArgsConstructor, @Data, @Slf4j)
- **Docker Compose** (PostgreSQL для локальной разработки)

## Development Setup
1. Docker Desktop запущен + "Expose daemon on tcp://localhost:2375 without TLS" включено
2. `docker compose up -d` для PostgreSQL
3. `./gradlew bootRun` для запуска приложения (порт 8080)
4. `./gradlew test` для запуска тестов (100+ юнит-тестов)
5. Swagger UI: `http://localhost:8080/swagger-ui/index.html`
6. IntelliJ HTTP Client файлы в `http/` для ручного тестирования

## Database
- PostgreSQL 16 (Docker)
- spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
- spring.jpa.hibernate.ddl-auto=update (таблицы создаются автоматически)
- Таблицы: users, roles, user_roles, clubs, teams, team_coaches, inventory, exercises, exercise_inventory, exercise_focuses, children, child_physical_stats, child_standards, standards, trainings, training_exercises, template_trainings, template_training_exercises, sets, set_exercises, training_programs

## Technical Constraints
- `spring.servlet.multipart.max-file-size=200KB` — увеличено до 500KB для картинок упражнений
- `spring.servlet.multipart.max-request-size=200KB` — аналогично
- Все файлы хранятся локально в `uploads/` (LocalFileStorage)
- JPA ddl-auto=update → при добавлении полей в entity БД обновляется автоматически

## Dependencies (build.gradle)
- Spring Boot Starters: Web, Security, Data JPA, Validation, Actuator
- PostgreSQL driver
- jjwt (io.jsonwebtoken)
- SpringDoc OpenAPI
- Lombok
- Testcontainers (JUnit 5 + PostgreSQL)
- JUnit 5, Mockito, AssertJ

## File Storage Configuration
```properties
app.upload.base-path=uploads
spring.servlet.multipart.max-file-size=500KB
spring.servlet.multipart.max-request-size=500KB
```

## FileType Enum
| Enum Value | Subdirectory | Max Size | Allowed MIME Types |
|------------|-------------|----------|-------------------|
| CLUB_LOGO | logos | 200 KB | image/jpeg, image/png, image/webp, image/svg+xml, image/gif |
| USER_AVATAR | avatars | 1 MB | image/jpeg, image/png, image/webp |
| TEAM_LOGO | logos/teams | 200 KB | image/jpeg, image/png, image/webp, image/svg+xml, image/gif |
| EXERCISE_PICTURE | exercises | 500 KB | image/jpeg, image/png, image/webp |

## Training Enums
| Enum | Значения |
|------|---------|
| ExerciseType | ICE, LAND |
| TrainingPart | BEGINNING, MIDDLE, END |
| Focus | STRENGTH, ENDURANCE, COORDINATION, SPEED, FLEXIBILITY, TECHNICAL |
| PreparationType | TECHNICAL, PHYSICAL, PSYCHOLOGICAL, TACTICAL |
| Intensity | MAXIMUM, SUBMAXIMUM, HIGH, MEDIUM, LOW |
| LoadLevel | MAXIMUM, SUBMAXIMUM, HIGH, MEDIUM, LOW |
| WorkMode | ANAEROBIC_ALACTIC, ANAEROBIC_MIXED, ANAEROBIC_LACTIC, AEROBIC, AEROBIC_RECOVERY |
| TrainingCycle | WEEK, MONTH, THREE_MONTHS, YEAR |

## Tool Usage Patterns
- **IDE tools preferred** (jetbrains-index MCP) for code navigation
- **Gradle** for build and test (not Maven)
- **Testcontainers** for integration tests (automatic Docker container lifecycle)
- **Mockito** for unit tests (service layer)
- **IntelliJ HTTP Client** for manual API testing (files in `http/`)
- **Postman Collection** (`JBFH.postman_collection.json`) for sharing API specs