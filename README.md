# JBFH — Hockey Schools Management API

**JBFH** — REST API для управления детскими хоккейными школами в Беларуси.  
Построен на Spring Boot 4 с JWT-аутентификацией, ролевой моделью доступа, загрузкой файлов и Swagger-документацией.

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
│   │   │   │   └── UserController.java       # CRUD /api/v1/users
│   │   │   ├── dto/
│   │   │   │   ├── ChangePasswordRequest.java
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
│   │   │       ├── UserInitService.java
│   │   │       └── UserService.java
│   │   ├── club/
│   │   │   ├── controller/ClubController.java   # CRUD /api/v1/clubs
│   │   │   ├── dto/ (CreateClubRequest, UpdateClubRequest, ClubResponse)
│   │   │   └── service/ClubService.java
│   │   ├── team/
│   │   │   ├── controller/TeamController.java   # CRUD /api/v1/clubs/{clubId}/teams
│   │   │   ├── dto/ (CreateTeamRequest, UpdateTeamRequest, TeamResponse, CoachResponse, AssignCoachRequest)
│   │   │   ├── entity/ (Team.java, TeamCoach.java)
│   │   │   ├── repository/ (TeamRepository.java, TeamCoachRepository.java)
│   │   │   └── service/TeamService.java
│   │   ├── inventory/
│   │   │   ├── controller/InventoryController.java   # CRUD /api/v1/inventory
│   │   │   ├── dto/ (CreateInventoryRequest, UpdateInventoryRequest, InventoryResponse)
│   │   │   ├── entity/Inventory.java
│   │   │   ├── repository/InventoryRepository.java
│   │   │   └── service/InventoryService.java
│   │   ├── exercise/
│   │   │   ├── controller/ExerciseController.java   # CRUD /api/v1/exercises
│   │   │   ├── dto/ (CreateExerciseRequest, UpdateExerciseRequest, ExerciseResponse)
│   │   │   ├── entity/ (Exercise.java, ExerciseInventory.java)
│   │   │   ├── repository/ (ExerciseRepository.java, ExerciseInventoryRepository.java)
│   │   │   └── service/ExerciseService.java
│   │   ├── storage/
│   │   │   ├── FileStorage.java                 # Интерфейс файлового хранилища
│   │   │   ├── LocalFileStorage.java            # Локальная реализация
│   │   │   └── enums/FileType.java              # Типы файлов (CLUB_LOGO, USER_AVATAR, TEAM_LOGO)
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
│   │   ├── auth/service/
│   │   │   ├── RoleServiceTest.java
│   │   │   ├── UserInitServiceTest.java
│   │   │   └── UserServiceTest.java
│   │   ├── club/service/
│   │   │   └── ClubServiceTest.java
│   │   ├── team/service/
│   │   │   └── TeamServiceTest.java
│   │   ├── common/exception/
│   │   │   └── GlobalExceptionHandlerTest.java
│   │   ├── config/
│   │   │   ├── JwtAuthenticationFilterTest.java
│   │   │   └── JwtServiceTest.java
│   │   └── storage/
│   │       ├── enums/
│   │       │   └── FileTypeTest.java
│   │       └── LocalFileStorageTest.java
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
| `GET` | `/swagger-ui/index.html` | Swagger UI |
| `GET` | `/api-docs` | OpenAPI JSON спецификация |

### Управление клубами

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/clubs` | `ROLE_ADMIN` | Создать клуб |
| `GET` | `/api/v1/clubs?page=0&size=10` | `ROLE_ADMIN`, `ROLE_METHODIST` | Список клубов (с пагинацией) |
| `GET` | `/api/v1/clubs/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Детали клуба |
| `PUT` | `/api/v1/clubs/{id}` | `ROLE_ADMIN` | Обновить клуб (address, description) |
| `POST` | `/api/v1/clubs/{id}/logo` | `ROLE_ADMIN` | Загрузить/обновить логотип клуба |
| `GET` | `/api/v1/clubs/{id}/logo` | `ROLE_ADMIN`, `ROLE_METHODIST` | Получить логотип клуба |

### Управление пользователями

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/users` | `ROLE_ADMIN`, `ROLE_CLUB` | Создать пользователя |
| `GET` | `/api/v1/users/{id}` | `ROLE_ADMIN` | Получить пользователя по ID |
| `PUT` | `/api/v1/users/me/password` | Любой аутентифицированный | Сменить свой пароль (требуется oldPassword) |
| `PUT` | `/api/v1/users/{id}/password` | `ROLE_ADMIN` | Сменить пароль любому пользователю (админ) |

### Управление командами

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/clubs/{clubId}/teams` | `ROLE_CLUB` | Создать команду |
| `GET` | `/api/v1/clubs/{clubId}/teams?page=0&size=18` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Список команд (пагинация, дефолт сортировка year DESC) |
| `GET` | `/api/v1/clubs/{clubId}/teams/{id}` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Детали команды |
| `PUT` | `/api/v1/clubs/{clubId}/teams/{id}` | `ROLE_CLUB` | Обновить команду |
| `PATCH` | `/api/v1/clubs/{clubId}/teams/{id}/active` | `ROLE_CLUB` | Активировать/деактивировать |
| `POST` | `/api/v1/clubs/{clubId}/teams/{id}/logo` | `ROLE_CLUB` | Загрузить/обновить логотип |
| `GET` | `/api/v1/clubs/{clubId}/teams/{id}/logo` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Получить логотип |
| `GET` | `/api/v1/clubs/{clubId}/teams/{id}/coaches` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Список тренеров команды |
| `POST` | `/api/v1/clubs/{clubId}/teams/{id}/coaches` | `ROLE_CLUB` | Назначить тренера |
| `DELETE` | `/api/v1/clubs/{clubId}/teams/{id}/coaches/{userId}` | `ROLE_CLUB` | Убрать тренера |

### Конструктор: инвентарь

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/inventory` | Все аутентифицированные | Создать инвентарь (админ/методист → без клуба; остальные → свой клуб) |
| `GET` | `/api/v1/inventory?page=0&size=20&active=true` | Все аутентифицированные | Список (админ/методист — все; остальные — общий + своего клуба) |
| `GET` | `/api/v1/inventory/{id}` | Все аутентифицированные | Детали |
| `PUT` | `/api/v1/inventory/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Обновить |
| `PATCH` | `/api/v1/inventory/{id}/active` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Деактивировать/активировать |

### Конструктор: упражнения

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `GET` | `/api/v1/exercises/types` | Публичный | Список типов упражнений (ICE, LAND) |
| `POST` | `/api/v1/exercises` | Все аутентифицированные | Создать упражнение (обязательно type: ICE/LAND) |
| `GET` | `/api/v1/exercises?page=0&size=20&active=true&type=ICE` | Все аутентифицированные | Список (фильтр по типу опционально) |
| `GET` | `/api/v1/exercises/{id}` | Все аутентифицированные | Детали |
| `PUT` | `/api/v1/exercises/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Обновить |
| `PATCH` | `/api/v1/exercises/{id}/active` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Деактивировать/активировать |
| `POST` | `/api/v1/exercises/{id}/picture` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Загрузить картинку |
| `GET` | `/api/v1/exercises/{id}/picture` | Все аутентифицированные | Получить картинку |

### Локации клубов

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/clubs/{clubId}/locations` | `ROLE_CLUB` | Создать локацию |
| `GET` | `/api/v1/clubs/{clubId}/locations?includeInactive=false` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Список локаций (без пагинации) |
| `GET` | `/api/v1/clubs/{clubId}/locations/{id}` | Те же | Детали локации |
| `PUT` | `/api/v1/clubs/{clubId}/locations/{id}` | `ROLE_CLUB` | Обновить локацию |
| `PATCH` | `/api/v1/clubs/{clubId}/locations/{id}/active` | `ROLE_CLUB` | Деактивировать/активировать |

### Примеры (демонстрация ролей)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `GET` | `/api/v1/admin/dashboard` | `ROLE_ADMIN` | Админ-панель |
| `GET` | `/api/v1/admin/stats` | `ROLE_ADMIN` | Статистика системы |
| `GET` | `/api/v1/coach/my-team` | `ROLE_COACH`, `ROLE_MAIN_COACH` | Моя команда |
| `GET` | `/api/v1/coach/schedule` | `ROLE_COACH`, `ROLE_MAIN_COACH` | Расписание |

---

## Фиксированные роли

При старте приложения автоматически создаются 6 ролей:

- `ROLE_ADMIN` — администратор системы (без клуба)
- `ROLE_CLUB` — представитель клуба
- `ROLE_METHODIST` — методист (без клуба)
- `ROLE_CLUB_METHODIST` — клубный методист
- `ROLE_COACH` — тренер
- `ROLE_MAIN_COACH` — главный тренер

### Привязка к клубу

- **Без клуба**: `ROLE_ADMIN`, `ROLE_METHODIST`
- **С клубом** (обязательно): `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH`

---

## FileStorage — система хранения файлов

Файлы (логотипы клубов, аватарки пользователей) хранятся через абстракцию `FileStorage`:

- **FileStorage** — интерфейс с методами `save()`, `delete()`, `getResource()`, `validate()`
- **LocalFileStorage** — реализация хранения в локальной файловой системе (папка `uploads/`)
- **FileType** — enum с типами файлов и их ограничениями:

| Тип | Подпапка | Макс. размер | Разрешённые MIME-типы |
|-----|----------|-------------|----------------------|
| `CLUB_LOGO` | `uploads/logos/` | 200 KB | image/jpeg, image/png, image/webp, image/svg+xml, image/gif |
| `USER_AVATAR` | `uploads/avatars/` | 1 MB | image/jpeg, image/png, image/webp |
| `TEAM_LOGO` | `uploads/logos/teams/` | 200 KB | image/jpeg, image/png, image/webp, image/svg+xml, image/gif |
| `EXERCISE_PICTURE` | `uploads/exercises/` | 500 KB | image/jpeg, image/png, image/webp |

---

## Default Admin User

При первом запуске автоматически создаётся администратор:

| Логин | Пароль | Роль |
|-------|--------|------|
| `admin` | `admin123` | `ROLE_ADMIN` |

---

## Swagger UI

После запуска приложения Swagger UI доступен по адресу:

```
http://localhost:8080/swagger-ui/index.html
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

### Загрузка файлов

| Параметр | Значение |
|----------|----------|
| `app.upload.base-path` | `uploads` |
| `spring.servlet.multipart.max-file-size` | `200KB` |
| `spring.servlet.multipart.max-request-size` | `200KB` |

---

## Логирование

Уровень логирования для пакета `com.par.jbfh` — `DEBUG`.  
Логи выводятся в консоль (stdout).