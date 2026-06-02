# JBFH Project Context

## Overview
Backend API for managing children's hockey schools in Belarus (JBFH = Jumalaista Bändiä Finska Hockey? Actually this is just the project name). 
Tech: Spring Boot 4.0.6 + Java 25 + PostgreSQL + JPA + Spring Security + OAuth2 Resource Server

## Architecture
```
com.par.jbfh/
├── auth/          — Аутентификация, авторизация, пользователи, роли
├── club/          — Управление клубами (CRUD, логотипы)
├── team/          — Управление командами (CRUD, логотипы, назначение тренеров)
├── storage/       — Файловое хранилище (интерфейс + LocalFileStorage + FileType enum)
├── config/        — Spring Security, JWT, Swagger конфигурации
├── example/       — Примеры контроллеров для демонстрации @Secured
└── common/        — Общие утилиты, exception handler
```

## Authentication
- **JWT (HS256)** — симметричный ключ
- Фильтр: `JwtAuthenticationFilter` — парсит Bearer token из заголовка Authorization
- Кастомный `UserPrincipal` extends `User` (Spring Security) с полем `userId`
- После аутентификации в SecurityContext лежит `UserPrincipal`

## Authorization via @Secured
Используется `@EnableMethodSecurity(securedEnabled = true)`. Аннотация `@Secured` на методах контроллеров:
```java
@Secured("ROLE_ADMIN")                          // только админ
@Secured({"ROLE_COACH", "ROLE_MAIN_COACH"})     // тренер или главный тренер
@Secured({"ROLE_ADMIN", "ROLE_CLUB"})           // админ или клуб
```

## Fixed Roles (6 roles, stored in DB)
- `ROLE_ADMIN` — администратор системы (без клуба)
- `ROLE_CLUB` — представитель клуба
- `ROLE_METHODIST` — методист (без клуба)
- `ROLE_CLUB_METHODIST` — клубный методист
- `ROLE_COACH` — тренер
- `ROLE_MAIN_COACH` — главный тренер

Roles are initialized at startup via `RoleService.initRoles()`.

## Entities
- **User** — id, username, password (BCrypt), email, enabled, club (nullable, ManyToOne), roles (Set, ManyToMany)
- **Role** — id, name (unique)
- **Club** — id, name (unique), address, description, logoPath, createdAt, updatedAt

Club-less roles: ROLE_ADMIN, ROLE_METHODIST
Club-required roles: ROLE_CLUB, ROLE_CLUB_METHODIST, ROLE_COACH, ROLE_MAIN_COACH

## API Endpoints

### Public (no auth)
- `POST /api/v1/auth/login` — логин, возвращает JWT
- `GET /api/v1/roles` — список всех ролей

### Clubs (package: club/)
- `POST /api/v1/clubs` — создание клуба (@Secured ROLE_ADMIN)
- `GET /api/v1/clubs?page=0&size=10` — список клубов с пагинацией (@Secured ROLE_ADMIN, ROLE_METHODIST)
- `GET /api/v1/clubs/{id}` — детали клуба (@Secured ROLE_ADMIN, ROLE_METHODIST)
- `PUT /api/v1/clubs/{id}` — обновление клуба (address, description) (@Secured ROLE_ADMIN)
- `POST /api/v1/clubs/{id}/logo` — загрузка/обновление логотипа (@Secured ROLE_ADMIN)
- `GET /api/v1/clubs/{id}/logo` — получение файла логотипа (@Secured ROLE_ADMIN, ROLE_METHODIST)
- DELETE отсутствует (запрещено)

### Users
- `POST /api/v1/users` — создание пользователя (@Secured ROLE_ADMIN, ROLE_CLUB)
- `GET /api/v1/users/{id}` — получение пользователя (@Secured ROLE_ADMIN)
- `PUT /api/v1/users/me/password` — смена своего пароля (любой authenticated, требуется oldPassword)
- `PUT /api/v1/users/{id}/password` — админская смена пароля (@Secured ROLE_ADMIN, oldPassword не требуется)

### Examples
- `GET /api/v1/admin/dashboard` — (@Secured ROLE_ADMIN)
- `GET /api/v1/admin/stats` — (@Secured ROLE_ADMIN)
- `GET /api/v1/coach/my-team` — (@Secured ROLE_COACH, ROLE_MAIN_COACH)
- `GET /api/v1/coach/schedule` — (@Secured ROLE_COACH, ROLE_MAIN_COACH)

## User Creation Logic
- **Admin** creates any user; if role requires club, admin specifies `clubId` in request
- **Club** (ROLE_CLUB) creates users auto-bound to their club; cannot create ADMIN or METHODIST

## Club Creation Logic
- Only **Admin** can create clubs (ROLE_ADMIN)
- Club is created independently, without a user
- User with ROLE_CLUB is created separately via `POST /api/v1/users`

## FileStorage — File Storage System (package: storage/)

### FileStorage interface
```java
public interface FileStorage {
    String save(MultipartFile file, UUID entityId, FileType fileType);
    void delete(String filePath);
    Resource getResource(String filePath);
    void validate(MultipartFile file, FileType fileType);
}
```

### LocalFileStorage
- Реализация `FileStorage` для локальной файловой системы
- Базовая директория: `uploads/` (настраивается через `app.upload.base-path`)
- Автоматически создаёт поддиректории для каждого FileType
- Генерирует уникальные имена файлов: `{entityId}_{timestamp}.{ext}`
- Валидирует размер и content-type перед сохранением

### FileType enum
| Тип | Подпапка | Макс. размер | Разрешённые MIME |
|-----|----------|-------------|-------------------|
| CLUB_LOGO | logos | 200 KB | image/jpeg, image/png, image/webp, image/svg+xml, image/gif |
| USER_AVATAR | avatars | 1 MB | image/jpeg, image/png, image/webp |

### Multipart configuration
```properties
spring.servlet.multipart.max-file-size=200KB
spring.servlet.multipart.max-request-size=200KB
app.upload.base-path=uploads
```

## Swagger
- UI: `/swagger-ui.html`
- API docs: `/api-docs`
- All protected endpoints have "Bearer Authentication" security scheme configured

## Database
- PostgreSQL via Docker Compose
- JPA ddl-auto=update (tables created automatically)
- Tables: users, roles, user_roles, clubs, teams, team_coaches

## Teams (package: team/)

### Entities
- **Team** — id, name, year (год рождения участников), description, logoPath, active (boolean, default true), club (ManyToOne), createdAt, updatedAt. Unique constraint: (name, club_id).
- **TeamCoach** — id, team (ManyToOne), user (ManyToOne). Unique constraint: (team_id, user_id). Таблица мапинга тренеров/главных тренеров на команды.

### API Endpoints (все требуют аутентификации)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/clubs/{clubId}/teams` | `ROLE_CLUB` | Создать команду |
| `GET` | `/api/v1/clubs/{clubId}/teams?page=0&size=18` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Список команд (пагинация, дефолт сортировка year DESC) |
| `GET` | `/api/v1/clubs/{clubId}/teams/{id}` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Детали команды |
| `PUT` | `/api/v1/clubs/{clubId}/teams/{id}` | `ROLE_CLUB` | Обновить команду |
| `PATCH` | `/api/v1/clubs/{clubId}/teams/{id}/active` | `ROLE_CLUB` | Активировать/деактивировать команду |
| `POST` | `/api/v1/clubs/{clubId}/teams/{id}/logo` | `ROLE_CLUB` | Загрузить/обновить логотип |
| `GET` | `/api/v1/clubs/{clubId}/teams/{id}/logo` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Получить логотип |
| `GET` | `/api/v1/clubs/{clubId}/teams/{id}/coaches` | `ROLE_CLUB`, `ROLE_CLUB_METHODIST` | Список тренеров команды |
| `POST` | `/api/v1/clubs/{clubId}/teams/{id}/coaches` | `ROLE_CLUB` | Назначить тренера |
| `DELETE` | `/api/v1/clubs/{clubId}/teams/{id}/coaches/{userId}` | `ROLE_CLUB` | Убрать тренера |

### Team Visibility Rules
- **ROLE_CLUB_METHODIST** — видит все команды всех клубов
- **ROLE_CLUB** — видит только команды своего клуба
- **ROLE_COACH, ROLE_MAIN_COACH** — видят только команды, к которым назначены через TeamCoach

### Team Creation Logic
- Only **Club** (ROLE_CLUB) can create teams
- Name must be unique within the club (unique constraint name + club_id)
- Year is mandatory — год рождения участников
- DELETE is not allowed; instead deactivate via `PATCH .../active?active=false`

## Testing
Test structure mirrors main:
```
src/test/java/com/par/jbfh/
├── auth/service/
│   ├── RoleServiceTest.java
│   ├── UserInitServiceTest.java
│   └── UserServiceTest.java
├── club/service/
│   └── ClubServiceTest.java
├── common/exception/
│   └── GlobalExceptionHandlerTest.java
├── config/
│   ├── JwtAuthenticationFilterTest.java
│   └── JwtServiceTest.java
└── storage/
    ├── enums/
    │   └── FileTypeTest.java
    └── LocalFileStorageTest.java
