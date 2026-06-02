package ru.student.recipes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class RecipeGuiApp extends JFrame {
    private final Db db;
    private final JLabel statusLabel = new JLabel("Готово");
    private final JTable recipeTable = new JTable();
    private final JTextArea detailsArea = new JTextArea();
    private final JTextField searchField = new JTextField();

    public static void main(String[] args) {
        String configPath = args.length > 0 ? args[0] : "config/app-sqlserver.properties";
        SwingUtilities.invokeLater(() -> {
            try {
                DbConfig config = DbConfig.load(configPath);
                Db db = new Db(config);
                try (Connection ignored = db.getConnection()) {
                    RecipeGuiApp app = new RecipeGuiApp(db);
                    app.setVisible(true);
                }
            } catch (Exception e) {
                showStartupError(e);
            }
        });
    }

    private static void showStartupError(Exception e) {
        JTextArea area = new JTextArea("Не удалось запустить приложение:\n" + e.getMessage());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(null, new JScrollPane(area), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public RecipeGuiApp(Db db) {
        this.db = db;
        setTitle("Приложение для рецептов — " + db.getConfig().getType());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        loadRecipes("");
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 0, 12));

        JLabel title = new JLabel("База рецептов");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchField.setToolTipText("Введите часть названия рецепта");
        JButton searchButton = new JButton("Найти");
        JButton resetButton = new JButton("Сброс");

        searchButton.addActionListener(e -> loadRecipes(searchField.getText().trim()));
        resetButton.addActionListener(e -> {
            searchField.setText("");
            loadRecipes("");
        });
        searchField.addActionListener(e -> loadRecipes(searchField.getText().trim()));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(searchButton);
        buttons.add(resetButton);

        searchPanel.add(new JLabel("Поиск:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(buttons, BorderLayout.EAST);
        panel.add(searchPanel, BorderLayout.CENTER);

        return panel;
    }

    private JSplitPane createCenter() {
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(new EmptyBorder(0, 12, 0, 4));

        recipeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeTable.setAutoCreateRowSorter(true);
        recipeTable.setRowHeight(24);
        recipeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int recipeId = getSelectedRecipeId();
                if (recipeId > 0) {
                    loadRecipeDetails(recipeId);
                }
            }
        });

        JPanel actions = new JPanel(new GridLayout(2, 3, 8, 8));
        JButton refreshButton = new JButton("Обновить");
        JButton addRecipeButton = new JButton("Добавить рецепт");
        JButton addReviewButton = new JButton("Добавить отзыв");
        JButton favoriteButton = new JButton("В избранное");
        JButton dirsButton = new JButton("Справочники");
        JButton deleteButton = new JButton("Удалить рецепт");

        refreshButton.addActionListener(e -> loadRecipes(searchField.getText().trim()));
        addRecipeButton.addActionListener(e -> showAddRecipeDialog());
        addReviewButton.addActionListener(e -> showAddReviewDialog());
        favoriteButton.addActionListener(e -> showAddFavoriteDialog());
        dirsButton.addActionListener(e -> showDirectoriesDialog());
        deleteButton.addActionListener(e -> deleteSelectedRecipe());

        actions.add(refreshButton);
        actions.add(addRecipeButton);
        actions.add(addReviewButton);
        actions.add(favoriteButton);
        actions.add(dirsButton);
        actions.add(deleteButton);

        left.add(new JScrollPane(recipeTable), BorderLayout.CENTER);
        left.add(actions, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(new EmptyBorder(0, 4, 0, 12));
        JLabel detailsTitle = new JLabel("Подробная информация");
        detailsTitle.setFont(detailsTitle.getFont().deriveFont(Font.BOLD, 16f));
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        right.add(detailsTitle, BorderLayout.NORTH);
        right.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.58);
        return splitPane;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(6, 12, 8, 12));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void loadRecipes(String searchText) {
        String sql = """
                SELECT r.recipe_id, r.title, c.name AS category_name, u.username AS author,
                       r.cooking_time_min, r.servings, r.difficulty,
                       COALESCE(ROUND(AVG(CAST(rv.rating AS FLOAT)), 2), 0) AS avg_rating
                FROM recipe r
                JOIN category c ON c.category_id = r.category_id
                JOIN app_user u ON u.user_id = r.author_id
                LEFT JOIN review rv ON rv.recipe_id = r.recipe_id
                WHERE LOWER(r.title) LIKE LOWER(?)
                GROUP BY r.recipe_id, r.title, c.name, u.username, r.cooking_time_min, r.servings, r.difficulty
                ORDER BY r.recipe_id
                """;
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + searchText + "%");
            try (ResultSet rs = statement.executeQuery()) {
                DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"ID", "Название", "Категория", "Автор", "Мин.", "Порций", "Сложность", "Рейтинг"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("recipe_id"),
                            rs.getString("title"),
                            rs.getString("category_name"),
                            rs.getString("author"),
                            rs.getInt("cooking_time_min"),
                            rs.getInt("servings"),
                            rs.getString("difficulty"),
                            rs.getDouble("avg_rating")
                    });
                }
                recipeTable.setModel(model);
                if (model.getRowCount() > 0) {
                    recipeTable.setRowSelectionInterval(0, 0);
                } else {
                    detailsArea.setText("Рецепты не найдены.");
                }
                setStatus("Загружено рецептов: " + model.getRowCount());
            }
        } catch (SQLException e) {
            showError("Ошибка загрузки рецептов", e);
        }
    }

    private void loadRecipeDetails(int recipeId) {
        StringBuilder text = new StringBuilder();
        String recipeSql = """
                SELECT r.title, r.description, c.name AS category_name, u.username AS author,
                       r.cooking_time_min, r.servings, r.difficulty
                FROM recipe r
                JOIN category c ON c.category_id = r.category_id
                JOIN app_user u ON u.user_id = r.author_id
                WHERE r.recipe_id = ?
                """;
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(recipeSql)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        detailsArea.setText("Рецепт не найден.");
                        return;
                    }
                    text.append(rs.getString("title")).append("\n");
                    text.append("=".repeat(Math.max(10, rs.getString("title").length()))).append("\n\n");
                    text.append("Категория: ").append(rs.getString("category_name")).append("\n");
                    text.append("Автор: ").append(rs.getString("author")).append("\n");
                    text.append("Время: ").append(rs.getInt("cooking_time_min")).append(" мин\n");
                    text.append("Порций: ").append(rs.getInt("servings")).append("\n");
                    text.append("Сложность: ").append(rs.getString("difficulty")).append("\n\n");
                    text.append("Описание:\n").append(rs.getString("description")).append("\n\n");
                }
            }

            text.append("Ингредиенты:\n");
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT i.name, ri.quantity, ri.unit, ri.note
                    FROM recipe_ingredient ri
                    JOIN ingredient i ON i.ingredient_id = ri.ingredient_id
                    WHERE ri.recipe_id = ?
                    ORDER BY i.name
                    """)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        String note = rs.getString("note") == null ? "" : " (" + rs.getString("note") + ")";
                        text.append("- ").append(rs.getString("name"))
                                .append(": ").append(rs.getDouble("quantity"))
                                .append(" ").append(rs.getString("unit")).append(note).append("\n");
                    }
                }
            }

            text.append("\nШаги приготовления:\n");
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT step_number, instruction
                    FROM recipe_step
                    WHERE recipe_id = ?
                    ORDER BY step_number
                    """)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        text.append(rs.getInt("step_number")).append(") ")
                                .append(rs.getString("instruction")).append("\n");
                    }
                }
            }

            text.append("\nОтзывы:\n");
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT u.username, rv.rating, rv.comment_text
                    FROM review rv
                    JOIN app_user u ON u.user_id = rv.user_id
                    WHERE rv.recipe_id = ?
                    ORDER BY rv.review_id
                    """)) {
                statement.setInt(1, recipeId);
                try (ResultSet rs = statement.executeQuery()) {
                    boolean hasReviews = false;
                    while (rs.next()) {
                        hasReviews = true;
                        text.append("- ").append(rs.getString("username"))
                                .append(": ").append(rs.getInt("rating"))
                                .append("/5 — ").append(rs.getString("comment_text")).append("\n");
                    }
                    if (!hasReviews) {
                        text.append("Пока нет отзывов.\n");
                    }
                }
            }
            detailsArea.setText(text.toString());
            detailsArea.setCaretPosition(0);
        } catch (SQLException e) {
            showError("Ошибка загрузки рецепта", e);
        }
    }

    private void showAddRecipeDialog() {
        JTextField authorId = new JTextField("1");
        JTextField categoryId = new JTextField("1");
        JTextField title = new JTextField();
        JTextArea description = new JTextArea(4, 25);
        JTextField time = new JTextField("30");
        JTextField servings = new JTextField("2");
        JComboBox<String> difficulty = new JComboBox<>(new String[]{"easy", "medium", "hard", "Легкий", "Средний", "Сложный"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("ID автора:"));
        panel.add(authorId);
        panel.add(new JLabel("ID категории:"));
        panel.add(categoryId);
        panel.add(new JLabel("Название:"));
        panel.add(title);
        panel.add(new JLabel("Описание:"));
        panel.add(new JScrollPane(description));
        panel.add(new JLabel("Время, мин:"));
        panel.add(time);
        panel.add(new JLabel("Порций:"));
        panel.add(servings);
        panel.add(new JLabel("Сложность:"));
        panel.add(difficulty);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить рецепт", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String sql = "INSERT INTO recipe(author_id, category_id, title, description, cooking_time_min, servings, difficulty) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(authorId.getText().trim()));
            statement.setInt(2, Integer.parseInt(categoryId.getText().trim()));
            statement.setString(3, title.getText().trim());
            statement.setString(4, description.getText().trim());
            statement.setInt(5, Integer.parseInt(time.getText().trim()));
            statement.setInt(6, Integer.parseInt(servings.getText().trim()));
            statement.setString(7, String.valueOf(difficulty.getSelectedItem()));
            statement.executeUpdate();
            setStatus("Рецепт добавлен");
            loadRecipes(searchField.getText().trim());
        } catch (Exception e) {
            showError("Не удалось добавить рецепт", e);
        }
    }

    private void showAddReviewDialog() {
        int recipeId = getSelectedRecipeId();
        JTextField recipeIdField = new JTextField(recipeId > 0 ? String.valueOf(recipeId) : "");
        JTextField userId = new JTextField("1");
        JComboBox<Integer> rating = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextArea comment = new JTextArea(4, 25);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("ID рецепта:"));
        panel.add(recipeIdField);
        panel.add(new JLabel("ID пользователя:"));
        panel.add(userId);
        panel.add(new JLabel("Оценка:"));
        panel.add(rating);
        panel.add(new JLabel("Комментарий:"));
        panel.add(new JScrollPane(comment));

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить отзыв", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String sql = "INSERT INTO review(recipe_id, user_id, rating, comment_text) VALUES (?, ?, ?, ?)";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(recipeIdField.getText().trim()));
            statement.setInt(2, Integer.parseInt(userId.getText().trim()));
            statement.setInt(3, (Integer) rating.getSelectedItem());
            statement.setString(4, comment.getText().trim());
            statement.executeUpdate();
            setStatus("Отзыв добавлен");
            loadRecipes(searchField.getText().trim());
            loadRecipeDetails(Integer.parseInt(recipeIdField.getText().trim()));
        } catch (Exception e) {
            showError("Не удалось добавить отзыв", e);
        }
    }

    private void showAddFavoriteDialog() {
        int recipeId = getSelectedRecipeId();
        JTextField recipeIdField = new JTextField(recipeId > 0 ? String.valueOf(recipeId) : "");
        JTextField userId = new JTextField("1");

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("ID пользователя:"));
        panel.add(userId);
        panel.add(new JLabel("ID рецепта:"));
        panel.add(recipeIdField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавить в избранное", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String sql = "INSERT INTO favorite(user_id, recipe_id) VALUES (?, ?)";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(userId.getText().trim()));
            statement.setInt(2, Integer.parseInt(recipeIdField.getText().trim()));
            statement.executeUpdate();
            setStatus("Рецепт добавлен в избранное");
        } catch (Exception e) {
            showError("Не удалось добавить в избранное", e);
        }
    }

    private void showDirectoriesDialog() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Пользователи", new JScrollPane(createTable("SELECT user_id AS ID, username AS Имя, email AS Email FROM app_user ORDER BY user_id")));
        tabs.addTab("Категории", new JScrollPane(createTable("SELECT category_id AS ID, name AS Название, description AS Описание FROM category ORDER BY category_id")));
        tabs.addTab("Ингредиенты", new JScrollPane(createTable("SELECT ingredient_id AS ID, name AS Название, default_unit AS Ед_изм FROM ingredient ORDER BY ingredient_id")));
        JDialog dialog = new JDialog(this, "Справочники", true);
        dialog.add(tabs);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTable createTable(String sql) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return new JTable(buildTableModel(rs));
        } catch (SQLException e) {
            showError("Ошибка загрузки справочника", e);
            return new JTable();
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
        }
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }
        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void deleteSelectedRecipe() {
        int recipeId = getSelectedRecipeId();
        if (recipeId <= 0) {
            JOptionPane.showMessageDialog(this, "Сначала выберите рецепт.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Удалить выбранный рецепт и связанные данные?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteByRecipeId(connection, "favorite", recipeId);
                deleteByRecipeId(connection, "review", recipeId);
                deleteByRecipeId(connection, "recipe_step", recipeId);
                deleteByRecipeId(connection, "recipe_ingredient", recipeId);
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM recipe WHERE recipe_id = ?")) {
                    statement.setInt(1, recipeId);
                    statement.executeUpdate();
                }
                connection.commit();
                setStatus("Рецепт удалён");
                loadRecipes(searchField.getText().trim());
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showError("Не удалось удалить рецепт", e);
        }
    }

    private void deleteByRecipeId(Connection connection, String table, int recipeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE recipe_id = ?")) {
            statement.setInt(1, recipeId);
            statement.executeUpdate();
        }
    }

    private int getSelectedRecipeId() {
        int row = recipeTable.getSelectedRow();
        if (row < 0 || recipeTable.getModel().getRowCount() == 0) return -1;
        int modelRow = recipeTable.convertRowIndexToModel(row);
        Object value = recipeTable.getModel().getValueAt(modelRow, 0);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private void showError(String title, Exception e) {
        JTextArea area = new JTextArea(e.getMessage());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setRows(8);
        area.setColumns(45);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title, JOptionPane.ERROR_MESSAGE);
        setStatus(title);
    }
}
