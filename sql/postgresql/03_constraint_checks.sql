-- Эти запросы должны завершиться ошибкой. Запускайте по одному.
-- 1. Неверный email
-- INSERT INTO app_user(username, email, password_hash) VALUES ('bad', 'bad-email', 'hash');

-- 2. Отрицательное время приготовления
-- INSERT INTO recipe(author_id, category_id, title, cooking_time_min, servings, difficulty) VALUES (1, 1, 'Ошибка', -10, 2, 'easy');

-- 3. Оценка вне диапазона
-- INSERT INTO review(recipe_id, user_id, rating, comment_text) VALUES (1, 3, 10, 'Ошибка');

-- 4. Повторный отзыв одного пользователя на один рецепт
-- INSERT INTO review(recipe_id, user_id, rating, comment_text) VALUES (1, 2, 3, 'Повтор');

-- 5. Количество ингредиента меньше или равно нулю
-- INSERT INTO recipe_ingredient(recipe_id, ingredient_id, quantity, unit) VALUES (1, 2, 0, 'g');
