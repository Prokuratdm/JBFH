# JBFH Project Context

## Overview
Backend API for managing children's hockey schools in Belarus.
Tech: Spring Boot 4.0.6 + Java 25 + PostgreSQL + JPA + Spring Security + OAuth2 Resource Server

## Architecture
```
auth/ — Аутентификация, пользователи, роли
club/ — Клубы (CRUD, логотипы)
team/ — Команды (CRUD, тренеры)
inventory/ — Инвентарь
exercise/ — Упражнения (ICE/LAND)
location/ — Локации клубов
child/ — Дети (профили, физические показатели, нормативы)
standard/ — Эталонные нормативы
training/ — Тренировки (упражнения, шаблоны, программы)
storage/ — Файловое хранилище
config/ — Security, JWT, Swagger
common/ — Exception handler
example/ — Демо-контроллеры
```

## API — последние добавленные эндпоинты

### Auth
| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `GET` | `/api/v1/auth/me` | Любой аутентифицированный | Информация о текущем пользователе |

> **Полный контекст API и модели данных:** смотри `memory-bank/*.md`
> - `memory-bank/systemPatterns.md` — архитектура, паттерны, компоненты
> - `memory-bank/progress.md` — полный список всех эндпоинтов по модулям
> - `memory-bank/activeContext.md` — текущий фокус, недавние изменения, следующие шаги
> - `er-diagram.puml` — ER-диаграмма всех сущностей

## Таблицы БД
users, roles, user_roles, clubs, teams, team_coaches, inventory, exercises, exercise_inventory, locations, children, child_physical_stats, child_standards, standards, trainings, training_exercises, template_trainings, template_training_exercises, training_programs