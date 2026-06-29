# Active Context: JBFH

## Текущий фокус
Стабилизация модуля training после DDD-рефакторинга: калькулятор вынесен в отдельный контроллер, упражнения тренингов перенесены в TrainingController, OAuth2-вход через Swagger Authorize.

## Недавние изменения (последние 10)

1. **2026-06-29** — Swagger OAuth2: добавлен `POST /api/v1/auth/oauth2/token`, кнопка Authorize принимает логин/пароль
2. **2026-06-29** — Training exercises CRUD перенесён из отдельного контроллера в `TrainingController`; создан `CalculatorController` (`/api/v1/calculator`) с 2 GET-ручками; `TrainingExerciseController` удалён
3. **2026-06-29** — TemplateTraining: убран `POST .../apply/{teamId}`, добавлен CRUD упражнений шаблона (POST/GET/PUT/DELETE `/api/v1/template-trainings/{id}/exercises`)
4. **2026-06-29** — DDD-рефакторинг: ExerciseCalculator вынесен из entity в @Service; добавлен агрегат Set; убраны @PrePersist/@PreUpdate из TrainingExercise и TemplateTrainingExercise
5. **2026-06-28** — Переработаны формулы расчёта TrainingExercise по таблицам из Python-кода; Intensity расширен до 5 значений; WorkMode заменён на ANAEROBIC_*/AEROBIC_*; добавлено поле repetitions; калькуляционные ручки переделаны с POST на GET
6. **2026-06-15** — Exercise расширен 3 enum-полями: TrainingPart, Focus (множественный), PreparationType; ручки /training-parts, /focuses, /preparation-types; фильтры в getAll
7. **2026-06-09** — Исправлен баг duplicate key в ExerciseService.update()
8. **2026-06-06** — GET /api/v1/clubs/{id}/logo стал публичным; GET /api/v1/users с фильтрацией и пагинацией
9. **2026-06-06** — GET /api/v1/auth/me
10. **2026-06-03** — Memory bank, child/, standard/, training/ — 41 новый эндпоинт

## Следующие шаги
- Написать тесты для ExerciseCalculator, SetService, TemplateTrainingService
- Создать HTTP-файлы для новых эндпоинтов (http/sets.http, http/calculator.http, http/template-trainings.http)
- Обновить Postman-коллекцию
- Обновить cline/context.md

## Активные решения
- **DDD-агрегаты** — Training, TemplateTraining, Set — каждый со своей таблицей, своими exercises, чёткими FK
- **ExerciseCalculator** — утилитный @Service. Переиспользуется тремя агрегатами и CalculatorController
- **CalculatorController** — отдельный контроллер `/api/v1/calculator` с 2 GET-ручками (без @Secured)
- **OAuth2 Password Flow** — Swagger Authorize через `POST /api/v1/auth/oauth2/token`
- **Мягкое удаление** — active boolean (кроме child, training, set)
- **Спецификации JPA** — для гибкой фильтрации упражнений
- **record DTO** — все response DTO
- **Enum endpoints** — /types, /training-parts, /focuses, /preparation-types

## Важные паттерны
- Модуль: controller/ → service/ → repository/ → entity/ + dto/ + enums/
- @Secured на всех методах (кроме калькулятора)
- Расчёт параметров — в сервисе перед save, через ExerciseCalculator
- Тесты: Mockito + SecurityContextHolder

## Инсайты
- Вынос расчётов из @PrePersist в сервис → тестируемость + переиспользование
- @Modifying(clearAutomatically = true) решает duplicate key при delete+insert
- distinct() на inventoryIds защищает от дубликатов
- @ElementCollection с EnumType.STRING для множественного enum (Focus)
- OAuth2 token endpoint с form-urlencoded для Swagger Authorize