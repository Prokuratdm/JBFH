# Progress: JBFH

## Что работает

### Auth
- [x] JWT аутентификация (HS256)
- [x] 6 фиксированных ролей, авто-инициализация при старте
- [x] Default admin (admin/admin123)
- [x] Логин, создание пользователей, смена пароля
- [x] Привязка пользователей к клубам (для клубных ролей)
- [x] Обновление lastSeenAt при каждом JWT-запросе
- [x] `GET /api/v1/auth/me` — получение информации о текущем пользователе

### Clubs
- [x] CRUD клубов
- [x] Загрузка/получение логотипов
- [x] Пагинация списка
- [x] Доступ: ADMIN (создание/редактирование), METHODIST (просмотр)

### Teams
- [x] CRUD команд (вложены в клубы)
- [x] Загрузка/получение логотипов
- [x] Назначение/удаление тренеров (TeamCoach)
- [x] Пагинация (дефолт 18, сортировка year DESC)
- [x] Правила видимости: METHODIST — все, CLUB — свой клуб, тренеры — только назначенные
- [x] Деактивация вместо удаления (active field)

### Inventory
- [x] CRUD инвентаря
- [x] Общий инвентарь (club is null) + клубный
- [x] Автоматическое назначение клуба для клубных ролей
- [x] Правила видимости: админы/методисты — всё, остальные — общий + свой клуб
- [x] Деактивация вместо удаления (active field)
- [x] Названия могут повторяться у разных клубов

### Exercises
- [x] CRUD упражнений
- [x] Поле type: ExerciseType enum (ICE/LAND)
- [x] Поля url (ссылка) и content (текст методики)
- [x] Фильтрация по типу в списке (опциональный параметр)
- [x] Загрузка/получение картинок (EXERCISE_PICTURE)
- [x] Привязка инвентаря (many-to-many через ExerciseInventory)
- [x] Глобальная уникальность названий
- [x] Правила видимости: админы/методисты — всё, остальные — общие + свой клуб
- [x] Деактивация вместо удаления (active field)
- [x] Public endpoint `GET /api/v1/exercises/types` — динамический список типов

### Children
- [x] CRUD детей (вложены в команды: `/api/v1/teams/{teamId}/children`) — 5 эндпоинтов
- [x] Gender enum (MALE, FEMALE)
- [x] Пагинация списка детей команды

### ChildPhysicalStats
- [x] История роста/веса детей (вложены в ребёнка: `/api/v1/children/{childId}/stats`) — 3 эндпоинта
- [x] Сортировка по дате DESC

### ChildStandards
- [x] Результаты сдачи нормативов (вложены в ребёнка: `/api/v1/children/{childId}/standards`) — 4 эндпоинта
- [x] Ссылка на Standard, отображение controlValue и unit

### Standards
- [x] CRUD эталонных нормативов (`/api/v1/standards`) — 5 эндпоинтов
- [x] StandardUnit enum (SECONDS, MINUTES, CENTIMETERS, METERS)
- [x] Фильтрация по birthYear и type (ExerciseType ICE/LAND)
- [x] Привязка к клубу (опционально)

### Trainings
- [x] CRUD тренировок (вложены в команду: `/api/v1/teams/{teamId}/trainings`) — 5 эндпоинтов
- [x] Фильтрация по диапазону дат (dateFrom, dateTo)
- [x] Поля: 3 задачи, sourceTemplate, createdBy
- [x] Пагинация (дефолт 10)

### TrainingExercises
- [x] Упражнения в тренировке (вложены: `/api/v1/trainings/{trainingId}/exercises`) — 4 эндпоинта
- [x] Intensity enum (HIGH, MEDIUM, LOW)
- [x] LoadLevel enum (MAXIMUM, SUBMAXIMUM, HIGH, MEDIUM, LOW)
- [x] WorkMode enum (INTERVAL, REPEATED, UNIFORM, ALTERNATING)
- [x] Авто-расчёт restDuration, workMode, totalTime через @PrePersist/@PreUpdate

### TemplateTrainings
- [x] CRUD эталонных тренировок (`/api/v1/template-trainings`) — 6 эндпоинтов
- [x] Метод apply: создание training из шаблона (POST /{id}/apply/{teamId})
- [x] Копирование задач, установка sourceTemplate

### TemplateTrainingExercises
- [x] Entity с авто-расчётом (аналогично TrainingExercise)
- [x] Методы управления в TemplateTrainingService

### TrainingPrograms
- [x] CRUD программ обучения (`/api/v1/training-programs`) — 5 эндпоинтов
- [x] TrainingCycle enum (WEEK, MONTH, THREE_MONTHS, YEAR)
- [x] Фильтрация по birthYear, loadLevel, cycle
- [x] Привязка к клубу (опционально)

### Storage
- [x] FileStorage интерфейс + LocalFileStorage реализация
- [x] FileType enum: CLUB_LOGO, USER_AVATAR, TEAM_LOGO, EXERCISE_PICTURE

### Testing
- [x] Юнит-тесты для всех сервисов (Mockito)
- [x] 100+ тестов, все проходят (BUILD SUCCESSFUL)
- [x] multipart.max-file-size увеличен до 500KB

## Что осталось сделать

### High Priority
- [ ] Написать тесты для новых модулей (ChildServiceTest, StandardServiceTest, TrainingServiceTest, TrainingExercise авто-расчёт)
- [ ] Создать HTTP-файлы для новых эндпоинтов (http/children.http, http/standards.http, http/trainings.http, http/template-trainings.http, http/training-programs.http)
- [ ] Обновить Postman-коллекцию для новых эндпоинтов
- [ ] Обновить `cline/context.md` — полное описание новых модулей

### Medium Priority
- [ ] Интеграционные тесты с Testcontainers для новых модулей
- [ ] Валидация прав доступа на уровне сервиса (сейчас только @Secured на контроллерах)
- [ ] Документация Swagger: добавить примеры request/response
- [ ] Удалить заглушки `example/controller/` или заменить реальными

### Low Priority / Future
- [ ] Расписания тренировок/матчей
- [ ] Турниры
- [ ] Статистика
- [ ] Рекомендательная система
- [ ] Журнал посещаемости
- [ ] Миграция на S3/MinIO для файлового хранилища

## Известные проблемы
- Нет обработки ошибок для больших файлов (400 вместо 413)
- TeamService.getTeamsByClub для тренеров использует `Specification` с `in` clause — может быть медленным на больших объёмах
- TrainingExerciseController работает напрямую с репозиториями (не через сервис) — рефакторинг при написании тестов

## Эволюция решений
1. **Было**: Логотипы как Base64 в БД → **Стало**: FileStorage с локальной ФС
2. **Было**: DELETE для удаления → **Стало**: active field + PATCH
3. **Было**: Кастомные @Query для всех фильтров → **Стало**: JpaSpecificationExecutor для Exercise (гибкость)
4. **Было**: Хардкод `List.of(ExerciseType.ICE.name(), ExerciseType.LAND.name())` → **Стало**: `Arrays.stream(ExerciseType.values())`
5. **Было**: 5 модулей (auth, club, team, inventory, exercise) → **Стало**: 10 модулей (+ child, standard, training + 3 доп. эндпоинта в существующих)
6. **Было**: JwtAuthenticationFilter без отслеживания активности → **Стало**: обновление lastSeenAt при каждом запросе