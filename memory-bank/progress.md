# Progress: JBFH

## Что работает

### Auth
- [x] JWT аутентификация (HS256)
- [x] 6 фиксированных ролей, авто-инициализация при старте
- [x] Default admin (admin/admin123)
- [x] Логин, создание пользователей, смена пароля
- [x] Привязка пользователей к клубам (для клубных ролей)
- [x] Обновление lastSeenAt при каждом JWT-запросе
- [x] `GET /api/v1/auth/me` — текущий пользователь
- [x] `GET /api/v1/users` — список с фильтрацией (clubId, role, username) и пагинацией
- [x] `POST /api/v1/auth/oauth2/token` — OAuth2 password flow для Swagger Authorize

### Clubs
- [x] CRUD клубов
- [x] Загрузка/получение логотипов (публичный GET)
- [x] Пагинация списка
- [x] Доступ: ADMIN (создание/редактирование), METHODIST (просмотр)

### Teams
- [x] CRUD команд (вложены в клубы)
- [x] Загрузка/получение логотипов
- [x] Назначение/удаление тренеров (TeamCoach)
- [x] Пагинация
- [x] Правила видимости
- [x] Деактивация вместо удаления (active field)

### Inventory
- [x] CRUD инвентаря
- [x] Общий инвентарь (club is null) + клубный
- [x] Правила видимости
- [x] Деактивация вместо удаления

### Exercises
- [x] CRUD упражнений
- [x] Поля: type (ICE/LAND), trainingPart, focuses (Set<Focus>), preparationType
- [x] Фильтрация по type, trainingPart, focus, preparationType
- [x] Загрузка/получение картинок
- [x] Привязка инвентаря (many-to-many)
- [x] Правила видимости
- [x] Деактивация вместо удаления
- [x] Enum endpoints: /types, /training-parts, /focuses, /preparation-types

### Trainings
- [x] CRUD тренировок (вложены в команду)
- [x] Фильтрация по диапазону дат
- [x] CRUD упражнений тренинга (POST/GET/PUT/DELETE `/.../{trainingId}/exercises`)

### TrainingExercises
- [x] Parameters: workDuration, intensity (5 значений), restDuration, explanationDuration, workMode (5 режимов), totalTime, loadLevel, repetitions
- [x] Расчёт через ExerciseCalculator

### Sets
- [x] CRUD сетов (`/api/v1/sets`)
- [x] CRUD упражнений сета (POST/GET/DELETE `/api/v1/sets/{id}/exercises`)

### TemplateTrainings
- [x] CRUD шаблонов (`/api/v1/template-trainings`)
- [x] CRUD упражнений шаблона (POST/GET/PUT/DELETE `/api/v1/template-trainings/{id}/exercises`)

### Calculator
- [x] `GET /api/v1/calculator/rest-and-mode` — расчёт без сохранения
- [x] `GET /api/v1/calculator/total-time` — полный расчёт без сохранения

### Children / Standards / TrainingPrograms
- [x] Полный CRUD

### Storage
- [x] FileStorage + LocalFileStorage

### Testing
- [x] 100+ тестов, BUILD SUCCESSFUL

## Что осталось сделать

### High Priority
- [ ] Написать тесты для ExerciseCalculator, SetService, TemplateTrainingService
- [ ] Создать HTTP-файлы для новых эндпоинтов (http/sets.http, http/calculator.http)
- [ ] Обновить Postman-коллекцию

### Medium Priority
- [ ] Интеграционные тесты с Testcontainers
- [ ] Swagger: примеры request/response
- [ ] Удалить заглушки example/controller/

### Low Priority
- [ ] Расписания, турниры, статистика
- [ ] Миграция на S3/MinIO

## Эволюция решений
1. Логотипы Base64 → FileStorage
2. DELETE → active field
3. Кастомные @Query → JpaSpecificationExecutor
4. 5 модулей → 10+ модулей
5. JwtAuthenticationFilter + lastSeenAt
6. @PrePersist/@PreUpdate → ExerciseCalculator @Service
7. Intensity 3 → 5 значений
8. WorkMode INTERVAL/... → ANAEROBIC_*/AEROBIC_*
9. TrainingExerciseController → CalculatorController + CRUD в TrainingController
10. TemplateTraining.apply → CRUD упражнений шаблона
11. Swagger Bearer → OAuth2 Password Flow