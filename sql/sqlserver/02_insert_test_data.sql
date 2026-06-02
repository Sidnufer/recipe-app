USE recipe_db;
GO

INSERT INTO dbo.app_user(username, email, password_hash) VALUES
(N'ivan', N'ivan@example.com', N'hash1'),
(N'anna', N'anna@example.com', N'hash2'),
(N'petr', N'petr@example.com', N'hash3'),
(N'maria', N'maria@example.com', N'hash4'),
(N'oleg', N'oleg@example.com', N'hash5');

INSERT INTO dbo.category(name, description) VALUES
(N'Салаты', N'Холодные блюда из овощей, мяса, рыбы и соусов'),
(N'Супы', N'Первые блюда'),
(N'Горячие блюда', N'Основные блюда'),
(N'Десерты', N'Сладкие блюда и выпечка'),
(N'Гарниры', N'Овощи, крупы, картофель');

INSERT INTO dbo.ingredient(name, default_unit) VALUES
(N'Куриное филе', N'g'),
(N'Картофель', N'g'),
(N'Морковь', N'g'),
(N'Лук', N'pcs'),
(N'Яйцо', N'pcs'),
(N'Мука', N'g'),
(N'Молоко', N'ml'),
(N'Сахар', N'g'),
(N'Соль', N'pinch'),
(N'Помидор', N'pcs');

INSERT INTO dbo.recipe(author_id, category_id, title, description, cooking_time_min, servings, difficulty) VALUES
(1, 1, N'Салат с курицей', N'Простой салат с куриным филе и овощами', 25, 2, N'easy'),
(2, 2, N'Куриный суп', N'Домашний суп с курицей и картофелем', 60, 4, N'medium'),
(3, 3, N'Картофельная запеканка', N'Сытное горячее блюдо', 70, 4, N'medium'),
(4, 4, N'Блины', N'Тонкие домашние блины', 40, 6, N'easy'),
(5, 5, N'Овощное пюре', N'Нежный гарнир из картофеля и моркови', 35, 3, N'easy');

INSERT INTO dbo.recipe_ingredient(recipe_id, ingredient_id, quantity, unit, note) VALUES
(1, 1, 250, N'g', N'отварить'), (1, 10, 2, N'pcs', NULL), (1, 9, 1, N'pinch', NULL),
(2, 1, 300, N'g', NULL), (2, 2, 400, N'g', NULL), (2, 3, 100, N'g', NULL), (2, 4, 1, N'pcs', NULL),
(3, 2, 700, N'g', NULL), (3, 5, 2, N'pcs', NULL), (3, 7, 150, N'ml', NULL),
(4, 5, 3, N'pcs', NULL), (4, 6, 250, N'g', NULL), (4, 7, 500, N'ml', NULL), (4, 8, 30, N'g', NULL),
(5, 2, 500, N'g', NULL), (5, 3, 200, N'g', NULL), (5, 9, 1, N'pinch', NULL);

INSERT INTO dbo.recipe_step(recipe_id, step_number, instruction) VALUES
(1, 1, N'Отварить куриное филе и нарезать кубиками.'),
(1, 2, N'Нарезать помидоры, смешать ингредиенты, посолить.'),
(2, 1, N'Сварить куриный бульон.'),
(2, 2, N'Добавить картофель, морковь и лук.'),
(2, 3, N'Варить до готовности овощей.'),
(3, 1, N'Нарезать картофель тонкими ломтиками.'),
(3, 2, N'Смешать молоко и яйца, залить картофель.'),
(3, 3, N'Запекать в духовке до готовности.'),
(4, 1, N'Смешать яйца, молоко, муку и сахар.'),
(4, 2, N'Жарить блины на разогретой сковороде.'),
(5, 1, N'Отварить картофель и морковь.'),
(5, 2, N'Размять овощи в пюре, добавить соль.');

INSERT INTO dbo.review(recipe_id, user_id, rating, comment_text) VALUES
(1, 2, 5, N'Быстро и вкусно'),
(2, 1, 4, N'Хороший домашний суп'),
(3, 4, 5, N'Очень сытно'),
(4, 5, 5, N'Получились тонкие блины'),
(5, 3, 4, N'Подходит как гарнир');

INSERT INTO dbo.favorite(user_id, recipe_id) VALUES
(1, 2), (2, 1), (3, 4), (4, 3), (5, 5);
GO
