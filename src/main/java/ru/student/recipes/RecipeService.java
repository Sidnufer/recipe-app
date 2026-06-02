package ru.student.recipes;

import java.sql.*;
import java.util.Scanner;

public class RecipeService {
    private final Db db;

    public RecipeService(Db db) {
        this.db = db;
    }

    public void showAllRecipes() {
        String sql = """
                SELECT r.recipe_id, r.title, c.name AS category_name, u.username AS author,
                       r.cooking_time_min, r.servings, r.difficulty,
                       COALESCE(ROUND(AVG(rv.rating), 2), 0) AS avg_rating
                FROM recipe r
                JOIN category c ON c.category_id = r.category_id
                JOIN app_user u ON u.user_id = r.author_id
                LEFT JOIN review rv ON rv.recipe_id = r.recipe_id
                GROUP BY r.recipe_id, r.title, c.name, u.username, r.cooking_time_min, r.servings, r.difficulty
                ORDER BY r.recipe_id
                """;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            System.out.println("\nСПИСОК РЕЦЕПТОВ");
            while (rs.next()) {
                System.out.printf("%d. %s | %s | автор: %s | %d мин | порций: %d | сложность: %s | рейтинг: %.2f%n",
                        rs.getInt("recipe_id"),
                        rs.getString("title"),
                        rs.getString("category_name"),
                        rs.getString("author"),
                        rs.getInt("cooking_time_min"),
                        rs.getInt("servings"),
                        rs.getString("difficulty"),
                        rs.getDouble("avg_rating"));
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void showRecipeDetails(Scanner scanner) {
        int recipeId = readInt(scanner, "Введите id рецепта: ");
        String recipeSql = """
                SELECT r.title, r.description, c.name AS category_name, u.username AS author,
                       r.cooking_time_min, r.servings, r.difficulty
                FROM recipe r
                JOIN category c ON c.category_id = r.category_id
                JOIN app_user u ON u.user_id = r.author_id
                WHERE r.recipe_id = ?
                """;
        String ingredientsSql = """
                SELECT i.name, ri.quantity, ri.unit, ri.note
                FROM recipe_ingredient ri
                JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
                WHERE ri.recipe_id = ?
                ORDER BY i.name
                """;
        String stepsSql = """
                SELECT step_number, instruction
                FROM recipe_step
                WHERE recipe_id = ?
                ORDER BY step_number
                """;

        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(recipeSql)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Рецепт не найден.");
                        return;
                    }
                    System.out.println("\n" + rs.getString("title"));
                    System.out.println("Категория: " + rs.getString("category_name"));
                    System.out.println("Автор: " + rs.getString("author"));
                    System.out.println("Время: " + rs.getInt("cooking_time_min") + " мин");
                    System.out.println("Порций: " + rs.getInt("servings"));
                    System.out.println("Сложность: " + rs.getString("difficulty"));
                    System.out.println("Описание: " + rs.getString("description"));
                }
            }

            System.out.println("\nИнгредиенты:");
            try (PreparedStatement statement = connection.prepareStatement(ingredientsSql)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String note = rs.getString("note") == null ? "" : " (" + rs.getString("note") + ")";
                        System.out.printf("- %s: %.2f %s%s%n",
                                rs.getString("name"), rs.getDouble("quantity"), rs.getString("unit"), note);
                    }
                }
            }

            System.out.println("\nШаги:");
            try (PreparedStatement statement = connection.prepareStatement(stepsSql)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("%d) %s%n", rs.getInt("step_number"), rs.getString("instruction"));
                    }
                }
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void searchRecipes(Scanner scanner) {
        System.out.print("Введите часть названия: ");
        String text = scanner.nextLine().trim();
        String sql = """
                SELECT r.recipe_id, r.title, c.name AS category_name, r.cooking_time_min
                FROM recipe r
                JOIN category c ON c.category_id = r.category_id
                WHERE LOWER(r.title) LIKE LOWER(?)
                ORDER BY r.title
                """;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + text + "%");
            try (ResultSet rs = statement.executeQuery()) {
                System.out.println("\nРезультаты поиска:");
                while (rs.next()) {
                    System.out.printf("%d. %s | %s | %d мин%n",
                            rs.getInt("recipe_id"), rs.getString("title"),
                            rs.getString("category_name"), rs.getInt("cooking_time_min"));
                }
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void addRecipe(Scanner scanner) {
        int authorId = readInt(scanner, "ID автора: ");
        int categoryId = readInt(scanner, "ID категории: ");
        System.out.print("Название рецепта: ");
        String title = scanner.nextLine().trim();
        System.out.print("Описание: ");
        String description = scanner.nextLine().trim();
        int time = readInt(scanner, "Время приготовления, мин: ");
        int servings = readInt(scanner, "Количество порций: ");
        System.out.print("Сложность (easy/medium/hard): ");
        String difficulty = scanner.nextLine().trim();

        String sql = """
                INSERT INTO recipe(author_id, category_id, title, description, cooking_time_min, servings, difficulty)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, authorId);
            statement.setInt(2, categoryId);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setInt(5, time);
            statement.setInt(6, servings);
            statement.setString(7, difficulty);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Рецепт добавлен. ID = " + keys.getInt(1));
                } else {
                    System.out.println("Рецепт добавлен.");
                }
            }
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void addReview(Scanner scanner) {
        int recipeId = readInt(scanner, "ID рецепта: ");
        int userId = readInt(scanner, "ID пользователя: ");
        int rating = readInt(scanner, "Оценка от 1 до 5: ");
        System.out.print("Комментарий: ");
        String comment = scanner.nextLine().trim();
        String sql = "INSERT INTO review(recipe_id, user_id, rating, comment_text) VALUES (?, ?, ?, ?)";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipeId);
            statement.setInt(2, userId);
            statement.setInt(3, rating);
            statement.setString(4, comment);
            statement.executeUpdate();
            System.out.println("Отзыв добавлен.");
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void addFavorite(Scanner scanner) {
        int userId = readInt(scanner, "ID пользователя: ");
        int recipeId = readInt(scanner, "ID рецепта: ");
        String sql = "INSERT INTO favorite(user_id, recipe_id) VALUES (?, ?)";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, recipeId);
            statement.executeUpdate();
            System.out.println("Рецепт добавлен в избранное.");
        } catch (SQLException e) {
            printError(e);
        }
    }

    public void showDirectories() {
        try (Connection connection = db.getConnection()) {
            System.out.println("\nПользователи:");
            printSimpleTable(connection, "SELECT user_id, username FROM app_user ORDER BY user_id", "user_id", "username");
            System.out.println("\nКатегории:");
            printSimpleTable(connection, "SELECT category_id, name FROM category ORDER BY category_id", "category_id", "name");
            System.out.println("\nИнгредиенты:");
            printSimpleTable(connection, "SELECT ingredient_id, name FROM ingredient ORDER BY ingredient_id", "ingredient_id", "name");
        } catch (SQLException e) {
            printError(e);
        }
    }

    private void printSimpleTable(Connection connection, String sql, String idColumn, String nameColumn) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                System.out.printf("%d. %s%n", rs.getInt(idColumn), rs.getString(nameColumn));
            }
        }
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    private void printError(SQLException e) {
        System.out.println("Ошибка базы данных: " + e.getMessage());
    }
}
