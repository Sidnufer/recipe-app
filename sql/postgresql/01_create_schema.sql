DROP TABLE IF EXISTS favorite CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS recipe_step CASCADE;
DROP TABLE IF EXISTS recipe_ingredient CASCADE;
DROP TABLE IF EXISTS ingredient CASCADE;
DROP TABLE IF EXISTS recipe CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

CREATE TABLE app_user (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_email CHECK (position('@' in email) > 1)
);

CREATE TABLE category (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE recipe (
    recipe_id SERIAL PRIMARY KEY,
    author_id INTEGER NOT NULL REFERENCES app_user(user_id) ON DELETE RESTRICT,
    category_id INTEGER NOT NULL REFERENCES category(category_id) ON DELETE RESTRICT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    cooking_time_min INTEGER NOT NULL,
    servings INTEGER NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_recipe_time CHECK (cooking_time_min BETWEEN 1 AND 1440),
    CONSTRAINT chk_recipe_servings CHECK (servings BETWEEN 1 AND 50),
    CONSTRAINT chk_recipe_difficulty CHECK (difficulty IN ('easy','medium','hard')),
    CONSTRAINT uq_recipe_author_title UNIQUE (author_id, title)
);

CREATE TABLE ingredient (
    ingredient_id SERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    default_unit VARCHAR(20) NOT NULL,
    CONSTRAINT chk_default_unit CHECK (default_unit IN ('g','kg','ml','l','pcs','tbsp','tsp','pinch'))
);

CREATE TABLE recipe_ingredient (
    recipe_id INTEGER NOT NULL REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    ingredient_id INTEGER NOT NULL REFERENCES ingredient(ingredient_id) ON DELETE RESTRICT,
    quantity NUMERIC(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    note VARCHAR(255),
    PRIMARY KEY (recipe_id, ingredient_id),
    CONSTRAINT chk_ri_quantity CHECK (quantity > 0),
    CONSTRAINT chk_ri_unit CHECK (unit IN ('g','kg','ml','l','pcs','tbsp','tsp','pinch'))
);

CREATE TABLE recipe_step (
    recipe_id INTEGER NOT NULL REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    step_number INTEGER NOT NULL,
    instruction VARCHAR(1000) NOT NULL,
    PRIMARY KEY (recipe_id, step_number),
    CONSTRAINT chk_step_number CHECK (step_number > 0)
);

CREATE TABLE review (
    review_id SERIAL PRIMARY KEY,
    recipe_id INTEGER NOT NULL REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    rating INTEGER NOT NULL,
    comment_text VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_review_user_recipe UNIQUE (recipe_id, user_id)
);

CREATE TABLE favorite (
    user_id INTEGER NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    recipe_id INTEGER NOT NULL REFERENCES recipe(recipe_id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, recipe_id)
);
