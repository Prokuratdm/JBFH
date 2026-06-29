# Active Context: JBFH

## Текущий фокус
DDD-рефакторинг модуля training: выделен утилитный ExerciseCalculator, добавлен новый агрегат Set (сеты упражнений для фаз тренировки), расчётные формулы вынесены из entity в сервис.

## Недавние изменения (последние 10)

1. **2026-06-29** — Рефакторинг TrainingExercise: ExerciseCalculator вынесен из entity, добавлен агрегат Set (entity + репозиторий + DTO + сервис + контроллер), убраны @PrePersist/@PreUpdate из TrainingExercise и TemplateTrainingExercise
2. **2026-06-28** — IntelliJ IDEA Ultimate
3. **2026-06-28** — Переработаны формулы расчёта TrainingExercise (restDuration/workMode/totalTime) по таблицам из Python-кода; Intensity расширен до 5 значений, WorkMode заменён на ANAEROBIC_*/AEROBIC_*; добавлено поле repetitions; калькуляционные ручки переделаны с POST на GET
4. **2026-06-15** — Exercise расширен 3 новыми enum-полями: TrainingPart, Focus (множественный), PreparationType; добавлены ручки /training-parts, /focuses, /preparation-types; добавлены фильтры в getAll
5. **2026-06-09** — Исправлен баг duplicate key в ExerciseService.update() (ExerciseInventory — @Modifying + clearAutomatically + distinct)
6. **2026-06-06** — GET /api/v1/clubs/{id}/logo стал публичным (убран @Secured, добавлен в permitAll)
7. **2026-06-06** — Добавлен эндпоинт GET /api/v1/users с фильтрацией (clubId, role, username) и пагинацией; UserRepository расширен JpaSpecificationExecutor
8. **2026-06-06** — Добавлен эндпоинт GET /api/v1/auth/me для получения информации о текущем пользователе из JWT токена
9. **2026-06-03** — Memory bank обновлён после завершения всех блоков реализации
10. **2026-06-03** — Добавлены модули: child/, standard/, training/ — 41 новый эндпоинт

## Следующие шаги
- Написать тесты для ExerciseCalculator, SetService
- Создать HTTP-файлы для новых эндпоинтов (http/sets.http)
- Создать HTTP-файлы для новых эндпоинтов (http/children.http, http/standards.http, http/trainings.http, http/template-trainings.http, http/training-programs.http)
- Обновить Postman-коллекцию для новых эндпоинтов
- Обновить cline/context.md — полное описание новых модулей
- Удалить заглушки example/controller/ или заменить реальными

## Активные решения
- **DDD-агрегаты** — Training, TemplateTraining, Set — каждый со своей таблицей, своими exercises, чёткими FK (подход A)
- **ExerciseCalculator** — утилитный @Service для расчёта restDuration, workMode, totalTime. Переиспользуется всеми тремя агрегатами и калькуляционными ручками
- **Мягкое удаление** — все сущности используют active boolean вместо DELETE (кроме child, training, set — там жёсткое удаление)
- **Спецификации JPA** — для гибкой фильтрации (упражнения: active + type + trainingPart + focus + preparationType + club visibility)
- **record DTO** — все response DTO используют Java records
- **Enum endpoints** — GET /types, /training-parts, /focuses, /preparation-types возвращают динамический список значений enum через Arrays.stream()
- **Вложенные контроллеры** — children вложены в teams, stats/standards вложены в children, trainings вложены в teams, exercises вложены в trainings/sets/templates

## Важные паттерны
- Каждый модуль: controller/ → service/ → repository/ → entity/ + dto/
- Все методы контроллеров защищены @Secured
- Получение текущего пользователя через SecurityContextHolder → UserPrincipal.userId → userRepository.findById
- Проверка ролей через hasRole(user, "ROLE_...")
- Тесты: Mockito + SecurityContextHolder.setContext(securityContext) для мокирования аутентификации
- Training.createdBy заполняется из текущего пользователя при создании
- Расчёт параметров упражнения — в сервисе перед save, а не в @PrePersist

## Инсайты
- Specification.where(null) амбигуозен в Spring Data JPA 4 → использовать (root, query, cb) -> cb.conjunction()
- Вынос расчётов из @PrePersist в сервис даёт лучшую тестируемость и переиспользование
- @Modifying(clearAutomatically = true) решает проблему duplicate key при delete+insert в одном батче
- distinct() на inventoryIds защищает от дубликатов в запросе
- JpaSpecificationExecutor + Specification даёт гибкую фильтрацию без взрыва методов репозитория
- uniqueConstraints на @Table лучше, чем отдельные constraints в БД — JPA создаёт их автоматически при ddl-auto=update
- @ElementCollection с EnumType.STRING — простой способ хранить множественный enum без отдельной entity (Focus в Exercise)