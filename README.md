# Менеджер задач (Java)

[![hexlet-check](https://github.com/AlexFrankiv/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/AlexFrankiv/java-project-99/actions)
[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=AlexFrankiv_java-project-99&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AlexFrankiv_java-project-99)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=AlexFrankiv_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=AlexFrankiv_java-project-99)

На практике узнаете про проектирование баз данных, связи между сущностями, PaaS, ORM, мониторинг ошибок, Swagger, фреймворк Spring.

Учебный проект, разработанный в рамках курса [Java-разработчик на Хекслете](https://ru.hexlet.io/programs/java). 
Полноценное REST API для менеджера задач с поддержкой ролей, JWT-аутентификацией, фильтрацией и интерактивной документацией.

Учебный проект Хекслета: https://ru.hexlet.io/programs/java
Как это должно работать: https://files.hexlet.app/a/xg6yxv

###  Живое демо
Приложение задеплоено на Render: [https://java-project-99-2sxt.onrender.com](https://java-project-99-2sxt.onrender.com)

### Интерактивная документация API (Swagger UI)
Вы можете изучить и протестировать все эндпоинты API прямо в браузере, не используя Postman:
*   **На продакшене:** [https://java-project-99-2sxt.onrender.com/swagger-ui/index.html](https://java-project-99-2sxt.onrender.com/swagger-ui/index.html)
*   **Локально (после запуска):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Стек

- Java
- Spring Boot 3, Spring Security, Spring Data JPA
- PostgreSQL (Prod), H2 (Test)
- JWT (JSON Web Tokens)
- MapStruct
-  Swagger
- Sentry (Bugsink)
- Gradle
- Docker
- React (предоставлен Hexlet, собирается через Vite)

### Функционал
*   **JWT-аутентификация:** Защита эндпоинтов, разделение ролей (Администратор и обычный пользователь).
*   **Пользователи (CRUD):** Регистрация, редактирование, удаление (с проверкой прав: обычный юзер может править только себя).
*   **Задачи (CRUD):** Создание задач с привязкой к статусу, меткам и исполнителю.
*   **Статусы (CRUD):** Управление статусами задач (Draft, ToReview, ToBeFixed и т.д.).
*   **Метки (CRUD):** Управление метками (Many-to-Many связь с задачами).
*   **Фильтрация:** Поиск задач по подстроке в названии, статусу, метке и исполнителю.

## Установка

<!-- Опишите установку: клонирование, зависимости, переменные окружения -->

```bash
git clone https://github.com/AlexFrankiv/java-project-99.git
cd java-project-99
```

## Использование

<!-- Добавьте примеры запуска и запись asciinema — именно это смотрит работодатель -->
Для работы требуется **JDK 21** и **Node.js** (для сборки фронтенда).

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/AlexFrankiv/java-project-99.git
   cd java-project-99
2. Установите зависимости и соберите проект (включая сборку фронтенда):
  ```bash
  make setup
  ```
3. Запустите приложение (по умолчанию на порту 8080):
   ```bash
   make start
   ```
---

<details>
<summary>Автоматические тесты Хекслета</summary>

Тесты запускаются на каждый коммит. За запуск отвечает файл `.github/workflows/hexlet-check.yml` — не удаляйте и не переименовывайте ни его, ни репозиторий.

</details>

## О Хекслете

[Хекслет](https://ru.hexlet.io/) — школа программирования: авторские программы обучения с практикой, поддержкой наставников и реальными проектами, которые остаются в резюме. Этот репозиторий — один из таких проектов.
