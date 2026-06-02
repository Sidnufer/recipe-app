package ru.student.recipes;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Ставим внешний вид как у обычных Windows-приложений
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Если стиль не применился, приложение всё равно запустится
                System.out.println("Не удалось применить системный стиль интерфейса.");
            }

            // Запускаем графическое приложение
            RecipeGuiApp.main(args);
        });
    }
}