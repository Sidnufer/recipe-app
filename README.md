# Приложение для рецептов

Учебный проект по базам данных и Java/JDBC. Приложение хранит рецепты, категории, ингредиенты, шаги приготовления, отзывы пользователей и избранные рецепты.

## Используемые технологии

- Java 17
- JDBC
- Maven
- PostgreSQL
- MySQL
- DBeaver или другая программа для работы с БД
- GitHub или GitVerse для размещения проекта

## Структура проекта

```text
recipe_project/
├── config/
│   ├── app-postgres.properties
│   └── app-mysql.properties
├── docs/
│   ├── report.md
│   └── testing_plan.md
├── sql/
│   ├── postgresql/
│   │   ├── 01_create_schema.sql
│   │   ├── 02_insert_test_data.sql
│   │   └── 03_constraint_checks.sql
│   └── mysql/
│       ├── 01_create_schema.sql
│       ├── 02_insert_test_data.sql
│       └── 03_constraint_checks.sql
├── src/main/java/ru/student/recipes/
│   ├── App.java
│   ├── Db.java
│   ├── DbConfig.java
│   └── RecipeService.java
└── pom.xml
```

## Как развернуть PostgreSQL

1. Создайте базу данных `recipe_db`.
2. Откройте DBeaver.
3. Подключитесь к PostgreSQL.
4. Откройте SQL Editor.
5. Выполните файл `sql/postgresql/01_create_schema.sql`.
6. Выполните файл `sql/postgresql/02_insert_test_data.sql`.
7. Проверьте таблицы запросом:

```sql
SELECT * FROM recipe;
```

8. В файле `config/app-postgres.properties` укажите свои логин и пароль.

## Как развернуть MySQL

1. Создайте базу данных:

```sql
CREATE DATABASE recipe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE recipe_db;
```

2. Выполните файл `sql/mysql/01_create_schema.sql`.
3. Выполните файл `sql/mysql/02_insert_test_data.sql`.
4. В файле `config/app-mysql.properties` укажите свои логин и пароль.

## Запуск приложения

### Запуск с PostgreSQL

```bash
mvn clean compile exec:java -Dexec.args="config/app-postgres.properties"
```

### Запуск с MySQL

```bash
mvn clean compile exec:java -Dexec.args="config/app-mysql.properties"
```

Если запуск идет из Eclipse, нужно открыть проект как Maven Project и в аргументах запуска указать путь к файлу настроек, например:

```text
config/app-postgres.properties
```

## Что умеет приложение

1. Показывает список рецептов.
2. Показывает подробную карточку рецепта.
3. Ищет рецепт по названию.
4. Добавляет новый рецепт.
5. Добавляет отзыв к рецепту.
6. Добавляет рецепт в избранное.
7. Показывает справочники пользователей, категорий и ингредиентов.

## Проверка ограничений

Файл `03_constraint_checks.sql` содержит примеры ошибочных запросов. Их нужно запускать по одному. СУБД должна отклонить данные, которые нарушают ограничения: неверный email, отрицательное время приготовления, рейтинг вне диапазона, повторный отзыв и нулевое количество ингредиента.

## Запуск графического интерфейса Swing

В проект добавлен удобный графический интерфейс `RecipeGuiApp`.

Главный класс GUI:

```text
ru.student.recipes.RecipeGuiApp
```

Для запуска через Eclipse:

1. Открой `src/main/java/ru/student/recipes/RecipeGuiApp.java`.
2. Нажми по нему правой кнопкой.
3. Выбери `Run As -> Java Application`.
4. В `Run Configurations -> Arguments -> Program arguments` укажи нужный конфиг, например:

```text
config/app-sqlserver.properties
```

или:

```text
config/app-postgres.properties
```

В интерфейсе доступны: просмотр рецептов, поиск, подробная карточка рецепта, добавление рецепта, добавление отзыва, добавление в избранное, просмотр справочников и удаление рецепта.
