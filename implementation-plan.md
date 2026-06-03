# Implementation Plan: JBFH

План реализации на основе ER-диаграммы (`er-diagram.puml`), ТЗ и текущего состояния проекта. Каждый пункт содержит конкретный эндпоинт, метод HTTP, URL и роли.

---

## Что уже есть (реализовано)

- [x] **Auth** — JWT, 6 ролей, пользователи, клубы
- [x] **Clubs** — CRUD, логотипы
- [x] **Teams** — CRUD, логотипы, назначение тренеров (TeamCoach)
- [x] **Inventory** — CRUD, общий/клубный, деактивация
- [x] **Exercises** — CRUD, типы ICE/LAND, картинки, связь с инвентарём
- [x] **Locations** — CRUD, вложены в клубы
- [x] **Storage** — FileStorage интерфейс + LocalFileStorage
- [x] **Tests** — 98+ тестов

---

## Блок 1: Доработка существующих сущностей

### 1.1 Exercises — добавить поля url, content
- [x] 1.1.1 Добавить поля `url` (VARCHAR 500) и `content` (TEXT) в `Exercise.java`
- [x] 1.1.2 Обновить `CreateExerciseRequest.java` — добавить `url`, `content`
- [x] 1.1.3 Обновить `UpdateExerciseRequest.java` — добавить `url`, `content`
- [x] 1.1.4 Обновить `ExerciseResponse.java` — добавить `url`, `content`
- [x] 1.1.5 Обновить `ExerciseService.java` — маппинг новых полей
- [x] 1.1.6 Обновить тесты `ExerciseServiceTest.java` — покрыть новые поля

### 1.2 Users — добавить last_seen_at
- [x] 1.2.1 Добавить поле `last_seen_at` (TIMESTAMP, nullable) в `User.java`
- [x] 1.2.2 Обновить `JwtAuthenticationFilter.java` — обновлять `last_seen_at` при каждом запросе с JWT
- [x] 1.2.3 Обновить `UserResponse.java` — добавить `lastSeenAt`
- [x] 1.2.4 Обновить тесты `JwtAuthenticationFilterTest.java` — проверить обновление last_seen_at

### 1.3 Multipart — увеличить лимит до 500KB
- [x] 1.3.1 Изменить `spring.servlet.multipart.max-file-size=500KB` в `application.properties`
- [x] 1.3.2 Изменить `spring.servlet.multipart.max-request-size=500KB` в `application.properties`

---

## Блок 2: Children (дети)

### Эндпоинты (вложены в команду: `/api/v1/teams/{teamId}/children`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/teams/{teamId}/children` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Создать ребёнка |
| `GET` | `/api/v1/teams/{teamId}/children?page=0&size=20` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Список детей команды (пагинация) |
| `GET` | `/api/v1/teams/{teamId}/children/{id}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Детали ребёнка |
| `PUT` | `/api/v1/teams/{teamId}/children/{id}` | `ROLE_CLUB` | Обновить ребёнка |
| `DELETE` | `/api/v1/teams/{teamId}/children/{id}` | `ROLE_CLUB` | Удалить ребёнка |

### Шаги реализации
- [x] 2.1 Создать enum `Gender` — `src/main/java/com/par/jbfh/child/enums/Gender.java` (MALE, FEMALE)
- [x] 2.2 Создать entity `Child.java` — `src/main/java/com/par/jbfh/child/entity/` (поля: id, firstName, lastName, middleName, birthYear, gender, team)
- [x] 2.3 Создать repository `ChildRepository.java`
- [x] 2.4 Создать dto: `CreateChildRequest.java`, `UpdateChildRequest.java`, `ChildResponse.java`
- [x] 2.5 Создать service `ChildService.java`
- [x] 2.6 Создать controller `ChildController.java` с эндпоинтами из таблицы выше
- [ ] 2.7 Написать тесты `ChildServiceTest.java`
- [ ] 2.8 Создать HTTP-файл `http/children.http`
- [x] 2.9 Обновить `cline/context.md` — задокументировать модуль

---

## Блок 3: ChildPhysicalStats (история роста/веса)

### Эндпоинты (вложены в ребёнка: `/api/v1/children/{childId}/stats`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/children/{childId}/stats` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Добавить запись роста/веса |
| `GET` | `/api/v1/children/{childId}/stats` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | История измерений (без пагинации, сортировка date DESC) |
| `DELETE` | `/api/v1/children/{childId}/stats/{id}` | `ROLE_CLUB` | Удалить запись измерения |

### Шаги реализации
- [x] 3.1 Создать entity `ChildPhysicalStats.java` — `src/main/java/com/par/jbfh/child/entity/` (поля: id, child, height, weight, date)
- [x] 3.2 Создать repository `ChildPhysicalStatsRepository.java`
- [x] 3.3 Создать dto: `CreatePhysicalStatsRequest.java`, `PhysicalStatsResponse.java`
- [x] 3.4 Создать service `ChildPhysicalStatsService.java`
- [x] 3.5 Создать controller `ChildPhysicalStatsController.java` с эндпоинтами из таблицы выше
- [ ] 3.6 Написать тесты `ChildPhysicalStatsServiceTest.java`
- [ ] 3.7 Обновить HTTP-файл `http/children.http` — добавить эндпоинты статистики

---

## Блок 4: Standards (эталонные нормативы)

### Эндпоинты (`/api/v1/standards`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/standards` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB_METHODIST` | Создать норматив |
| `GET` | `/api/v1/standards?page=0&size=20&birthYear=&type=` | Все аутентифицированные | Список (фильтры: birthYear, type) |
| `GET` | `/api/v1/standards/{id}` | Все аутентифицированные | Детали норматива |
| `PUT` | `/api/v1/standards/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST`, `ROLE_CLUB_METHODIST` | Обновить норматив |
| `DELETE` | `/api/v1/standards/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Удалить норматив |

### Шаги реализации
- [x] 4.1 Создать enum `StandardUnit` — `src/main/java/com/par/jbfh/standard/enums/StandardUnit.java` (SECONDS, MINUTES, CENTIMETERS, METERS)
- [x] 4.2 Создать entity `Standard.java` — `src/main/java/com/par/jbfh/standard/entity/` (поля: id, name, type, birthYear, unit, controlValue, club)
- [x] 4.3 Создать repository `StandardRepository.java`
- [x] 4.4 Создать dto: `CreateStandardRequest.java`, `UpdateStandardRequest.java`, `StandardResponse.java`
- [x] 4.5 Создать service `StandardService.java`
- [x] 4.6 Создать controller `StandardController.java` с эндпоинтами из таблицы выше
- [ ] 4.7 Написать тесты `StandardServiceTest.java`
- [ ] 4.8 Создать HTTP-файл `http/standards.http`

---

## Блок 5: ChildStandards (результаты нормативов детей)

### Эндпоинты (вложены в ребёнка: `/api/v1/children/{childId}/standards`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/children/{childId}/standards` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Записать результат норматива |
| `GET` | `/api/v1/children/{childId}/standards` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Все результаты ребёнка |
| `PUT` | `/api/v1/children/{childId}/standards/{id}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Обновить результат |
| `DELETE` | `/api/v1/children/{childId}/standards/{id}` | `ROLE_CLUB` | Удалить результат |

### Шаги реализации
- [x] 5.1 Создать entity `ChildStandard.java` — `src/main/java/com/par/jbfh/child/entity/` (поля: id, child, standard, resultValue, date)
- [x] 5.2 Создать repository `ChildStandardRepository.java`
- [x] 5.3 Создать dto: `CreateChildStandardRequest.java`, `ChildStandardResponse.java`
- [x] 5.4 Создать service `ChildStandardService.java`
- [x] 5.5 Создать controller `ChildStandardController.java` с эндпоинтами из таблицы выше
- [ ] 5.6 Написать тесты `ChildStandardServiceTest.java`
- [ ] 5.7 Обновить HTTP-файл `http/children.http`

---

## Блок 6: Trainings (тренировки)

### Эндпоинты (вложены в команду: `/api/v1/teams/{teamId}/trainings`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/teams/{teamId}/trainings` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Создать тренировку |
| `GET` | `/api/v1/teams/{teamId}/trainings?page=0&size=10&dateFrom=&dateTo=` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Список (пагинация, фильтр по дате) |
| `GET` | `/api/v1/teams/{teamId}/trainings/{id}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Детали тренировки (с упражнениями) |
| `PUT` | `/api/v1/teams/{teamId}/trainings/{id}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Обновить тренировку |
| `DELETE` | `/api/v1/teams/{teamId}/trainings/{id}` | `ROLE_CLUB` | Удалить тренировку |

### Шаги реализации
- [x] 6.1 Создать entity `Training.java` — `src/main/java/com/par/jbfh/training/entity/` (поля: id, name, date, timeStart, timeEnd, location, team, picturePath, description, task1-3, sourceTemplate, createdBy)
- [x] 6.2 Создать repository `TrainingRepository.java`
- [x] 6.3 Создать dto: `CreateTrainingRequest.java`, `UpdateTrainingRequest.java`, `TrainingResponse.java`
- [x] 6.4 Создать service `TrainingService.java`
- [x] 6.5 Создать controller `TrainingController.java` с эндпоинтами из таблицы выше
- [ ] 6.6 Написать тесты `TrainingServiceTest.java`
- [ ] 6.7 Создать HTTP-файл `http/trainings.http`

---

## Блок 7: TrainingExercises (упражнения в тренировке)

### Эндпоинты (вложены в тренировку: `/api/v1/trainings/{trainingId}/exercises`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/trainings/{trainingId}/exercises` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Добавить упражнение в тренировку |
| `GET` | `/api/v1/trainings/{trainingId}/exercises` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Список упражнений (с авто-расчётными полями) |
| `PUT` | `/api/v1/trainings/{trainingId}/exercises/{id}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Обновить параметры упражнения |
| `DELETE` | `/api/v1/trainings/{trainingId}/exercises/{id}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Убрать упражнение из тренировки |

### Шаги реализации
- [x] 7.1 Создать enum `Intensity` — `src/main/java/com/par/jbfh/training/enums/Intensity.java` (HIGH, MEDIUM, LOW)
- [x] 7.2 Создать enum `LoadLevel` — `src/main/java/com/par/jbfh/training/enums/LoadLevel.java` (MAXIMUM, SUBMAXIMUM, HIGH, MEDIUM, LOW)
- [x] 7.3 Создать enum `WorkMode` — `src/main/java/com/par/jbfh/training/enums/WorkMode.java` (INTERVAL, REPEATED, UNIFORM, ALTERNATING)
- [x] 7.4 Создать entity `TrainingExercise.java` — `src/main/java/com/par/jbfh/training/entity/` (поля: id, training, exercise, workDuration, intensity, restDuration, explanationDuration, workMode, totalTime, loadLevel)
- [x] 7.5 Добавить логику авто-расчёта в TrainingExercise: `rest_duration`, `work_mode`, `total_time`
- [x] 7.6 Создать dto: `AddExerciseToTrainingRequest.java`, `TrainingExerciseResponse.java`
- [x] 7.7 Добавить метод добавления/удаления упражнений в `TrainingService`
- [x] 7.8 Создать controller `TrainingExerciseController.java` с эндпоинтами из таблицы выше
- [ ] 7.9 Написать тесты для логики авто-расчёта
- [ ] 7.10 Обновить HTTP-файл `http/trainings.http`

---

## Блок 8: TemplateTrainings (эталонные тренировки)

### Эндпоинты (`/api/v1/template-trainings`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/template-trainings` | `ROLE_ADMIN`, `ROLE_METHODIST` | Создать эталонную тренировку |
| `GET` | `/api/v1/template-trainings?page=0&size=10` | Все аутентифицированные | Список эталонных тренировок |
| `GET` | `/api/v1/template-trainings/{id}` | Все аутентифицированные | Детали (с упражнениями) |
| `PUT` | `/api/v1/template-trainings/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Обновить эталонную тренировку |
| `DELETE` | `/api/v1/template-trainings/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Удалить эталонную тренировку |
| `POST` | `/api/v1/template-trainings/{id}/apply/{teamId}` | `ROLE_CLUB`, `ROLE_COACH`, `ROLE_MAIN_COACH` | Применить шаблон → создать training для команды (копирование задач + упражнений) |

### Шаги реализации
- [x] 8.1 Создать entity `TemplateTraining.java` — `src/main/java/com/par/jbfh/training/entity/` (поля: id, name, description, picturePath, task1-3, club)
- [x] 8.2 Создать repository `TemplateTrainingRepository.java`
- [x] 8.3 Создать dto: `CreateTemplateTrainingRequest.java`, `TemplateTrainingResponse.java`
- [x] 8.4 Создать service `TemplateTrainingService.java`
- [x] 8.5 Создать controller `TemplateTrainingController.java` с эндпоинтами из таблицы выше
- [x] 8.6 Реализовать метод `apply`: копирование template → training (задачи, поле source_template_id)
- [ ] 8.7 Написать тесты `TemplateTrainingServiceTest.java`
- [ ] 8.8 Создать HTTP-файл `http/template-trainings.http`

---

## Блок 9: TemplateTrainingExercises (упражнения эталонной тренировки)

### Эндпоинты (вложены: `/api/v1/template-trainings/{templateId}/exercises`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/template-trainings/{templateId}/exercises` | `ROLE_ADMIN`, `ROLE_METHODIST` | Добавить упражнение в шаблон |
| `GET` | `/api/v1/template-trainings/{templateId}/exercises` | Все аутентифицированные | Список упражнений шаблона |
| `PUT` | `/api/v1/template-trainings/{templateId}/exercises/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Обновить параметры |
| `DELETE` | `/api/v1/template-trainings/{templateId}/exercises/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Убрать упражнение |

### Шаги реализации
- [x] 9.1 Создать entity `TemplateTrainingExercise.java` — `src/main/java/com/par/jbfh/training/entity/` (те же поля что TrainingExercise, но templateTraining вместо training)
- [x] 9.2 Добавить логику авто-расчёта (аналогично TrainingExercise)
- [ ] 9.3 Создать dto для template exercises
- [x] 9.4 Добавить методы управления упражнениями в `TemplateTrainingService`
- [ ] 9.5 Создать controller `TemplateTrainingExerciseController.java` с эндпоинтами из таблицы выше
- [ ] 9.6 Написать тесты

---

## Блок 10: TrainingPrograms (программы обучения)

### Эндпоинты (`/api/v1/training-programs`)

| Метод | URL | Роль | Описание |
|-------|-----|------|----------|
| `POST` | `/api/v1/training-programs` | `ROLE_ADMIN`, `ROLE_METHODIST` | Создать запись программы |
| `GET` | `/api/v1/training-programs?birthYear=&loadLevel=&cycle=` | Все аутентифицированные | Список (фильтры опциональны) |
| `GET` | `/api/v1/training-programs/{id}` | Все аутентифицированные | Детали записи |
| `PUT` | `/api/v1/training-programs/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Обновить запись |
| `DELETE` | `/api/v1/training-programs/{id}` | `ROLE_ADMIN`, `ROLE_METHODIST` | Удалить запись |

### Шаги реализации
- [x] 10.1 Создать enum `TrainingCycle` — `src/main/java/com/par/jbfh/training/enums/TrainingCycle.java` (WEEK, MONTH, THREE_MONTHS, YEAR)
- [x] 10.2 Создать entity `TrainingProgram.java` — `src/main/java/com/par/jbfh/training/entity/` (поля: id, birthYear, loadLevel, cycle, percentage, club)
- [x] 10.3 Создать repository `TrainingProgramRepository.java`
- [x] 10.4 Создать dto: `CreateTrainingProgramRequest.java`, `TrainingProgramResponse.java`
- [x] 10.5 Создать service `TrainingProgramService.java`
- [x] 10.6 Создать controller `TrainingProgramController.java` с эндпоинтами из таблицы выше
- [ ] 10.7 Написать тесты `TrainingProgramServiceTest.java`
- [ ] 10.8 Создать HTTP-файл `http/training-programs.http`

---

## Блок 11: Финализация

- [ ] 11.1 Обновить `README.md` — новые модули в структуре проекта, новые эндпоинты в таблице API
- [ ] 11.2 Обновить `cline/context.md` — полное описание новых модулей и эндпоинтов
- [ ] 11.3 Обновить `memory-bank/` — все файлы (activeContext, progress, systemPatterns, techContext)
- [ ] 11.4 Обновить `JBFH.postman_collection.json` — новые эндпоинты
- [ ] 11.5 Прогнать полный набор тестов `./gradlew test` — убедиться, что всё зелёное
- [ ] 11.6 Прогнать сборку `./gradlew build` — убедиться, что нет ошибок компиляции
- [ ] 11.7 Удалить заглушки `example/controller/` (AdminController, CoachController) или заменить реальными

---

## Сводка всех эндпоинтов

| Блок | Базовый URL | Кол-во эндпоинтов |
|------|-------------|-------------------|
| 2 — Children | `/api/v1/teams/{teamId}/children` | 5 |
| 3 — ChildPhysicalStats | `/api/v1/children/{childId}/stats` | 3 |
| 4 — Standards | `/api/v1/standards` | 5 |
| 5 — ChildStandards | `/api/v1/children/{childId}/standards` | 4 |
| 6 — Trainings | `/api/v1/teams/{teamId}/trainings` | 5 |
| 7 — TrainingExercises | `/api/v1/trainings/{trainingId}/exercises` | 4 |
| 8 — TemplateTrainings | `/api/v1/template-trainings` | 6 |
| 9 — TemplateTrainingExercises | `/api/v1/template-trainings/{templateId}/exercises` | 4 |
| 10 — TrainingPrograms | `/api/v1/training-programs` | 5 |
| **Всего новых эндпоинтов** | | **41** |

---

## Порядок выполнения

Рекомендуемый порядок: **1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11**

- Блоки 2–3 (дети) и 4–5 (нормативы) независимы друг от друга и от блока 6–10 (тренировки)
- Блок 6 зависит от блока 2 (Location уже есть)
- Блок 7 зависит от блока 6
- Блок 8–9 независимы от 2–7, но используют enum'ы из блока 7
- Блок 10 независим от всех, кроме enum'ов из блока 7