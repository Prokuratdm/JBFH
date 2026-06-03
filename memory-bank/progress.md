# Progress: JBFH

## Что работает

### Auth
- [x] JWT аутентификация (HS256)
- [x] 6 фиксированных ролей, авто-инициализация при старте
- [x] Default admin (admin/admin123)
- [x] Логин, создание пользователей, смена пароля
- [x] Привязка пользователей к клубам (для клубных ролей)

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
- [x] Фильтрация по типу в списке (опциональный параметр)
- [x] Загрузка/получение картинок (EXERCISE_PICTURE)
- [x] Привязка инвентаря (many-to-many через ExerciseInventory)
- [x] Глобальная уникальность названий
- [x] Правила видимости: админы/методисты — всё, остальные — общие + свой клуб
- [x] Деактивация вместо удаления (active field)
- [x] Public endpoint `GET /api/v1/exercises/types` — динамический список типов

### Storage
- [x] FileStorage интерфейс + LocalFileStorage реализация
- [x] FileType enum: CLUB_LOGO, USER_AVATAR, TEAM_LOGO, EXERCISE_PICTURE

### Testing
- [x] Юнит-тесты для всех сервисов (Mockito)
- [x] 98+ тестов, все проходят (BUILD SUCCESSFUL)

## Что осталось сделать

### High Priority
- [ ] Увеличить `spring.servlet.multipart.max-file-size` до 500KB (картинки упражнений)
- [ ] Обновить HTTP-файлы для новых эндпоинтов (inventory, exercises)
- [ ] Обновить Postman-коллекцию для новых эндпоинтов

### Medium Priority
- [ ] Интеграционные тесты с Testcontainers для новых модулей
- [ ] Валидация прав доступа на уровне сервиса (сейчас только @Secured на контроллерах)
- [ ] Документация Swagger: добавить примеры request/response

### Low Priority / Future
- [ ] Расписания тренировок/матчей
- [ ] Турниры
- [ ] Статистика
- [ ] Миграция на S3/MinIO для файлового хранилища

## Известные проблемы
- Нет обработки ошибок для больших файлов (400 вместо 413)
- `spring.servlet.multipart.max-file-size=200KB` не покрывает EXERCISE_PICTURE (500KB)
- TeamService.getTeamsByClub для тренеров использует `Specification` с `in` clause — может быть медленным на больших объёмах

## Эволюция решений
1. **Было**: Логотипы как Base64 в БД → **Стало**: FileStorage с локальной ФС
2. **Было**: DELETE для удаления → **Стало**: active field + PATCH
3. **Было**: Кастомные @Query для всех фильтров → **Стало**: JpaSpecificationExecutor для Exercise (гибкость)
4. **Было**: Хардкод `List.of(ExerciseType.ICE.name(), ExerciseType.LAND.name())` → **Стало**: `Arrays.stream(ExerciseType.values())`