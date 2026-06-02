USE recipe_db;
GO

-- Запускай проверки по одной. Каждая команда должна завершаться ошибкой ограничения.

-- 1. Неверный рейтинг: разрешено только от 1 до 5
INSERT INTO dbo.review(recipe_id, user_id, rating, comment_text)
VALUES (1, 3, 10, N'Неверная оценка');

-- 2. Неверная сложность: разрешены easy, medium, hard
INSERT INTO dbo.recipe(author_id, category_id, title, description, cooking_time_min, servings, difficulty)
VALUES (1, 1, N'Ошибочный рецепт', N'Проверка ограничения', 20, 2, N'very hard');

-- 3. Неверное время приготовления: должно быть от 1 до 1440 минут
INSERT INTO dbo.recipe(author_id, category_id, title, description, cooking_time_min, servings, difficulty)
VALUES (1, 1, N'Рецепт с нулевым временем', N'Проверка ограничения', 0, 2, N'easy');

-- 4. Дублирование категории: имя категории уникально
INSERT INTO dbo.category(name, description)
VALUES (N'Салаты', N'Повтор категории');

-- 5. Несуществующий автор: проверка внешнего ключа
INSERT INTO dbo.recipe(author_id, category_id, title, description, cooking_time_min, servings, difficulty)
VALUES (999, 1, N'Рецепт без автора', N'Проверка внешнего ключа', 20, 2, N'easy');
GO
