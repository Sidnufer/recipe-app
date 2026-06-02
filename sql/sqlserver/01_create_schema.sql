USE recipe_db;
GO

IF OBJECT_ID(N'dbo.favorite', N'U') IS NOT NULL DROP TABLE dbo.favorite;
IF OBJECT_ID(N'dbo.review', N'U') IS NOT NULL DROP TABLE dbo.review;
IF OBJECT_ID(N'dbo.recipe_step', N'U') IS NOT NULL DROP TABLE dbo.recipe_step;
IF OBJECT_ID(N'dbo.recipe_ingredient', N'U') IS NOT NULL DROP TABLE dbo.recipe_ingredient;
IF OBJECT_ID(N'dbo.ingredient', N'U') IS NOT NULL DROP TABLE dbo.ingredient;
IF OBJECT_ID(N'dbo.recipe', N'U') IS NOT NULL DROP TABLE dbo.recipe;
IF OBJECT_ID(N'dbo.category', N'U') IS NOT NULL DROP TABLE dbo.category;
IF OBJECT_ID(N'dbo.app_user', N'U') IS NOT NULL DROP TABLE dbo.app_user;
GO

CREATE TABLE dbo.app_user (
    user_id INT IDENTITY(1,1) NOT NULL,
    username NVARCHAR(50) NOT NULL,
    email NVARCHAR(120) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_app_user PRIMARY KEY (user_id),
    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT uq_app_user_email UNIQUE (email),
    CONSTRAINT chk_user_email CHECK (CHARINDEX(N'@', email) > 1)
);
GO

CREATE TABLE dbo.category (
    category_id INT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(60) NOT NULL,
    description NVARCHAR(255) NULL,
    CONSTRAINT pk_category PRIMARY KEY (category_id),
    CONSTRAINT uq_category_name UNIQUE (name)
);
GO

CREATE TABLE dbo.recipe (
    recipe_id INT IDENTITY(1,1) NOT NULL,
    author_id INT NOT NULL,
    category_id INT NOT NULL,
    title NVARCHAR(120) NOT NULL,
    description NVARCHAR(1000) NULL,
    cooking_time_min INT NOT NULL,
    servings INT NOT NULL,
    difficulty NVARCHAR(20) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_recipe PRIMARY KEY (recipe_id),
    CONSTRAINT fk_recipe_author FOREIGN KEY (author_id) REFERENCES dbo.app_user(user_id),
    CONSTRAINT fk_recipe_category FOREIGN KEY (category_id) REFERENCES dbo.category(category_id),
    CONSTRAINT chk_recipe_time CHECK (cooking_time_min BETWEEN 1 AND 1440),
    CONSTRAINT chk_recipe_servings CHECK (servings BETWEEN 1 AND 50),
    CONSTRAINT chk_recipe_difficulty CHECK (difficulty IN (N'easy', N'medium', N'hard')),
    CONSTRAINT uq_recipe_author_title UNIQUE (author_id, title)
);
GO

CREATE TABLE dbo.ingredient (
    ingredient_id INT IDENTITY(1,1) NOT NULL,
    name NVARCHAR(80) NOT NULL,
    default_unit NVARCHAR(20) NOT NULL,
    CONSTRAINT pk_ingredient PRIMARY KEY (ingredient_id),
    CONSTRAINT uq_ingredient_name UNIQUE (name),
    CONSTRAINT chk_default_unit CHECK (default_unit IN (N'g', N'kg', N'ml', N'l', N'pcs', N'tbsp', N'tsp', N'pinch'))
);
GO

CREATE TABLE dbo.recipe_ingredient (
    recipe_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit NVARCHAR(20) NOT NULL,
    note NVARCHAR(255) NULL,
    CONSTRAINT pk_recipe_ingredient PRIMARY KEY (recipe_id, ingredient_id),
    CONSTRAINT fk_ri_recipe FOREIGN KEY (recipe_id) REFERENCES dbo.recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_ri_ingredient FOREIGN KEY (ingredient_id) REFERENCES dbo.ingredient(ingredient_id),
    CONSTRAINT chk_ri_quantity CHECK (quantity > 0),
    CONSTRAINT chk_ri_unit CHECK (unit IN (N'g', N'kg', N'ml', N'l', N'pcs', N'tbsp', N'tsp', N'pinch'))
);
GO

CREATE TABLE dbo.recipe_step (
    recipe_id INT NOT NULL,
    step_number INT NOT NULL,
    instruction NVARCHAR(1000) NOT NULL,
    CONSTRAINT pk_recipe_step PRIMARY KEY (recipe_id, step_number),
    CONSTRAINT fk_step_recipe FOREIGN KEY (recipe_id) REFERENCES dbo.recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT chk_step_number CHECK (step_number > 0)
);
GO

CREATE TABLE dbo.review (
    review_id INT IDENTITY(1,1) NOT NULL,
    recipe_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comment_text NVARCHAR(1000) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_review PRIMARY KEY (review_id),
    CONSTRAINT fk_review_recipe FOREIGN KEY (recipe_id) REFERENCES dbo.recipe(recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES dbo.app_user(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_review_user_recipe UNIQUE (recipe_id, user_id)
);
GO

CREATE TABLE dbo.favorite (
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    added_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_favorite PRIMARY KEY (user_id, recipe_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES dbo.app_user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_recipe FOREIGN KEY (recipe_id) REFERENCES dbo.recipe(recipe_id) ON DELETE CASCADE
);
GO
