# Active Context: JBFH

## Текущий фокус
Масштабное расширение модели данных: добавлены модули Children, Standards, Trainings (с упражнениями и авто-расчётом), TemplateTrainings, TrainingPrograms. Реализована полная ER-модель из `er-diagram.puml`.

## Недавние изменения (последние 10)

1. **2026-06-03** — Memory bank обновлён после завершения всех блоков реализации
2. **2026-06-03** — Добавлены модули: `child/` (Children, ChildPhysicalStats, ChildStandards), `standard/` (Standards CRUD), `training/` (Trainings, TrainingExercises, TemplateTrainings, TrainingPrograms) — 41 новый эндпоинт
3. **2026-06-03** — Доработаны существующие сущности: Exercise (url, content), User (lastSeenAt), JwtAuthenticationFilter (обновление lastSeenAt + userRepository)
4. **2026-06-03** — `application.properties`: multipart лимит увеличен до 500KB
5. **2026-06-03** — `JwtAuthenticationFilterTest` исправлен — добавлен мок `userRepository`
6. **2026-06-03** — `implementation-plan.md` создан и обновлён — все пункты Блоков 1-10 отмечены выполненными
7. **2026-06-03** — `README.md` обновлён: добавлены модули `child/`, `standard/`, `training/` в структуру проекта
8. **2026-06-03** — `er-diagram.puml` доработана до полноценной модели (8 новых сущностей, 6 новых enum'ов, комментарии)
9. **2026-06-03** — Исправлен `getTypes()` — переписан на `Arrays.stream(ExerciseType.values()).map(ExerciseType::name).toList()`
10. **2026-06-03** — Добавлен enum `ExerciseType` (ICE, LAND), поле `type` в Exercise, фильтрация по типу

## Следующие шаги
- Написать тесты для новых модулей (ChildServiceTest, StandardServiceTest, TrainingServiceTest и др.)
- Создать HTTP-файлы для новых эндпоинтов (http/children.http, http/standards.http, http/trainings.http, http/template-trainings.http, http/training-programs.http)
- Обновить Postman-коллекцию для новых эндпоинтов
- Обновить `cline/context.md` — полное описание новых модулей
- Удалить заглушки `example/controller/` или заменить реальными

## Активные решения
- **Мягкое удаление** — все сущности используют `active` boolean вместо DELETE (кроме child, training — там жёсткое удаление)
- **Авто-расчёт** — TrainingExercise и TemplateTrainingExercise используют `@PrePersist`/`@PreUpdate` для расчёта `restDuration`, `workMode`, `totalTime`
- **Спецификации JPA** — для гибкой фильтрации (упражнения: active + type + club visibility)
- **record DTO** — все response DTO используют Java records
- **Enum endpoints** — `GET /types` возвращает динамический список значений enum через `Arrays.stream()`
- **Вложенные контроллеры** — children вложены в teams, stats/standards вложены в children, trainings вложены в teams, exercises вложены в trainings

## Важные паттерны
- Каждый модуль: `controller/` → `service/` → `repository/` → `entity/` + `dto/`
- Все методы контроллеров защищены `@Secured`
- Получение текущего пользователя через `SecurityContextHolder` → `UserPrincipal.userId` → `userRepository.findById`
- Проверка ролей через `hasRole(user, "ROLE_...")`
- Тесты: Mockito + `SecurityContextHolder.setContext(securityContext)` для мокирования аутентификации
- Training.createdBy заполняется из текущего пользователя при создании

## Инсайты
- `Specification.where(null)` амбигуозен в Spring Data JPA 4 → использовать `(root, query, cb) -> cb.conjunction()`
- `@PrePersist` + `@PreUpdate` в TrainingExercise позволяет авто-расчёту работать при создании и обновлении
- `JpaSpecificationExecutor` + `Specification` даёт гибкую фильтрацию без взрыва методов репозитория
- `uniqueConstraints` на `@Table` лучше, чем отдельные constraints в БД — JPA создаёт их автоматически при `ddl-auto=update`