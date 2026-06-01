# JBFH Project Context

## Overview
Backend API for managing children's hockey schools in Belarus (JBFH = Jumalaista Bändiä Finska Hockey? Actually this is just the project name). 
Tech: Spring Boot 4.0.6 + Java 25 + PostgreSQL + JPA + Spring Security + OAuth2 Resource Server

## Architecture
```
com.par.jbfh/
├── auth/          — Аутентификация, авторизация, пользователи, роли
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
- **User** — id, username, password (BCrypt), email, enabled, club (nullable), roles (Set)
- **Role** — id, name (unique)
- **Club** — id, name, createdAt

Club-less roles: ROLE_ADMIN, ROLE_METHODIST
Club-required roles: ROLE_CLUB, ROLE_CLUB_METHODIST, ROLE_COACH, ROLE_MAIN_COACH

## API Endpoints

### Public (no auth)
- `POST /api/v1/auth/login` — логин, возвращает JWT
- `GET /api/v1/roles` — список всех ролей

### Protected
- `POST /api/v1/users` — создание пользователя (@Secured ROLE_ADMIN, ROLE_CLUB)
- `GET /api/v1/users/{id}` — получение пользователя (@Secured ROLE_ADMIN)
- `GET /api/v1/admin/dashboard` — (@Secured ROLE_ADMIN)
- `GET /api/v1/admin/stats` — (@Secured ROLE_ADMIN)
- `GET /api/v1/coach/my-team` — (@Secured ROLE_COACH, ROLE_MAIN_COACH)
- `GET /api/v1/coach/schedule` — (@Secured ROLE_COACH, ROLE_MAIN_COACH)

## User Creation Logic
- **Admin** creates any user; if role requires club, admin specifies `clubId` in request
- **Club** (ROLE_CLUB) creates users auto-bound to their club; cannot create ADMIN or METHODIST

## Swagger
- UI: `/swagger-ui.html`
- API docs: `/api-docs`
- All protected endpoints have "Bearer Authentication" security scheme configured

## Database
- PostgreSQL via Docker Compose
- JPA ddl-auto=update (tables created automatically)
- Tables: users, roles, user_roles, clubs

## Testing
Test structure mirrors main:
```
src/test/java/com/par/jbfh/
├── auth/
│   ├── controller/
│   │   └── RoleControllerTest.java
│   └── service/
│       └── RoleServiceTest.java
└── example/
    └── controller/
        ├── AdminControllerTest.java
        └── CoachControllerTest.java