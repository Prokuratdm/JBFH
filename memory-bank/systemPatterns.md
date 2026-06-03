# System Patterns: JBFH

## Architecture
```
com.par.jbfh/
├── auth/          — Аутентификация, авторизация, пользователи, роли
├── club/          — Управление клубами (CRUD, логотипы)
├── team/          — Управление командами (CRUD, логотипы, назначение тренеров)
├── inventory/     — Инвентарь (CRUD, общий/клубный)
├── exercise/      — Упражнения (CRUD, картинки, привязка инвентаря, типы ICE/LAND)
├── storage/       — Файловое хранилище (интерфейс + LocalFileStorage + FileType enum)
├── config/        — Spring Security, JWT, Swagger
├── example/       — Примеры контроллеров для демонстрации @Secured
└── common/        — Exception handler
```

## Layer Pattern
Каждый модуль:
```
module/
├── controller/    — @RestController, @Secured, @Operation
├── service/       — @Service, @Transactional, бизнес-логика
├── repository/    — JpaRepository, кастомные @Query
├── dto/           — records (Response) + @Data (Request)
└── entity/        — @Entity, JPA
```

## Key Design Decisions

### Authentication
- JWT HS256, фильтр `JwtAuthenticationFilter`, парсит Bearer token
- `UserPrincipal` extends `User` с полем `userId`
- `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` → `UserPrincipal`

### Authorization
- `@EnableMethodSecurity(securedEnabled = true)` + `@Secured("ROLE_...")`
- Роли: ADMIN, CLUB, METHODIST, CLUB_METHODIST, COACH, MAIN_COACH
- Club-less: ADMIN, METHODIST
- Club-required: все остальные

### Soft Delete
- Все сущности: `active` boolean (default true)
- DELETE запрещён → PATCH `/{id}/active?active=false`

### Visibility Rules
- **Админы/методисты** → видят всё
- **Клубные роли** → общее (club is null) + своего клуба
- **Тренеры команд** → только назначенные команды (TeamCoach)

### File Storage
- Абстракция `FileStorage` (save, delete, getResource, validate)
- Реализация `LocalFileStorage` (локальная ФС)
- `FileType` enum: CLUB_LOGO, USER_AVATAR, TEAM_LOGO, EXERCISE_PICTURE

### Current User
```java
User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
    return userRepository.findById(principal.getUserId()).orElseThrow(...);
}
```

### Testing Pattern
- Mockito + `SecurityContextHolder.setContext(securityContext)` + `SecurityContextHolder.clearContext()`
- `UserPrincipal` создаётся мануально для мокирования аутентификации
- `when(securityContext.getAuthentication()).thenReturn(authentication)`
- `when(authentication.getPrincipal()).thenReturn(principal)`
- `when(userRepository.findById(user.getId())).thenReturn(Optional.of(user))`

## Component Relationships
```
Club 1──N Team
Club 1──N Inventory (nullable — общий)
Club 1──N Exercise (nullable — общее)
Team N──M User (через TeamCoach)
Exercise N──M Inventory (через ExerciseInventory)