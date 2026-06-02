DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS recipe_step;
DROP TABLE IF EXISTS recipe_ingredient;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS recipe;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_email CHECK (LOCATE('@', email) > 1)
);

CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE recipe (
    recipe_id INT AUTO_INCREMENT PRIMARY KEY,
    author_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    cooking_time_min INT NOT NULL,
    servings INT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipe_author FOREIGN KEY (author_id) REFERENCES app_user(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_recipe_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE RESTRICT,
    CONSTRAINT chk_recipe_time CHECK (cooking_time_min BETWEEN 1 AND 1440),
    CONSTRAINT chk_recipe_servings CHECK (servings BETWEEN 1 AND 50),
    CONSTRAINT chk_recipe_difficulty CHECK (difficulty IN ('easy','medium','hard')),
    CONSTRAINT uq_recipe_author_title UNIQUE (author_id, title)
);

CREATE TABLE ingredient (
    ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    default_unit VARCHAR(20) NOT NULL,
    CONSTRAINT chk_default_unit CHECK (default_unit IN ('g','kg','ml','l','pcs','tbsp','tsp','pinch'))
);

CREATE TABLE recipe_ingredient (
    recipe_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    note VARCHAR(255),
    PRIMARY KEY (recipe_id, ingredient_id),
    CONSTRAINT fk_ri_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_ri_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE RESTRICT,
    CONSTRAINT chk_ri_quantity CHECK (quantity > 0),
    CONSTRAINT chk_ri_unit CHECK (unit IN ('g','kg','ml','l','pcs','tbsp','tsp','pinch'))
);

CREATE TABLE recipe_step (
    recipe_id INT NOT NULL,
    step_number INT NOT NULL,
    instruction VARCHAR(1000) NOT NULL,
    PRIMARY KEY (recipe_id, step_number),
    CONSTRAINT fk_step_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT chk_step_number CHECK (step_number > 0)
);

CREATE TABLE review (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comment_text VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_review_user_recipe UNIQUE (recipe_id, user_id)
);

CREATE TABLE favorite (
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recipe_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_recipe FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id) ON DELETE CASCADE
);
