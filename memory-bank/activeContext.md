# Active Context: JBFH

## Текущий фокус
Завершён модуль «Конструктор упражнений» — добавлены инвентарь и упражнения с типами ICE/LAND.

## Недавние изменения (последние 10)

1. **2026-06-03** — Исправлен `getTypes()` — переписан на `Arrays.stream(ExerciseType.values()).map(ExerciseType::name).toList()`
2. **2026-06-03** — Добавлен enum `ExerciseType` (ICE, LAND), поле `type` в Exercise, фильтрация по типу в списке, ручка `GET /api/v1/exercises/types`
3. **2026-06-02** — Создан модуль `exercise` (упражнения) с CRUD, картинками, many-to-many связью с инвентарём
4. **2026-06-02** — Создан модуль `inventory` (инвентарь) — CRUD, общий/клубный, деактивация
5. **2026-06-02** — Созданы `.http` файлы для IntelliJ HTTP Client и `JBFH.postman_collection.json`
6. **2026-06-02** — Создан модуль `team` (команды) — CRUD, логотипы, назначение тренеров, видимость по ролям
7. **2026-06-02** — Обновлены README.md и cline/context.md
8. **Ранее** — Модули: auth (JWT, роли, пользователи), club (клубы), storage (файлы), config (Security, Swagger)

## Следующие шаги
- Обновить HTTP-файлы и Postman-коллекцию для новых эндпоинтов (inventory, exercises)
- Возможные фичи: расписания, турниры, статистика
- Обновить `spring.servlet.multipart.max-file-size` (сейчас 200KB, картинки упражнений 500KB — надо увеличить)

## Активные решения
- **Мягкое удаление** — все сущности используют `active` boolean вместо DELETE
- **Спецификации JPA** — для гибкой фильтрации (упражнения: active + type + club visibility)
- **record DTO** — `ExerciseResponse`, `InventoryResponse`, `TeamResponse`, `CoachResponse`
- **Enum endpoints** — `GET /types` возвращает динамический список значений enum через `Arrays.stream()`

## Важные паттерны
- Каждый модуль: `controller/` → `service/` → `repository/` → `entity/` + `dto/`
- Все методы контроллеров защищены `@Secured`
- Получение текущего пользователя через `SecurityContextHolder` → `UserPrincipal.userId` → `userRepository.findById`
- Проверка ролей через `hasRole(user, "ROLE_...")`
- Тесты: Mockito + `SecurityContextHolder.setContext(securityContext)` для мокирования аутентификации

## Инсайты
- `Specification.where(null)` амбигуозен в Spring Data JPA 4 → использовать `(root, query, cb) -> cb.conjunction()`
- IntelliJ HTTP Client удобнее Postman для локальной разработки (не нужен импорт, работает из IDE)
- `JpaSpecificationExecutor` + `Specification` даёт гибкую фильтрацию без взрыва методов репозитория
- `uniqueConstraints` на `@Table` лучше, чем отдельные constraints в БД — JPA создаёт их автоматически при `ddl-auto=update`