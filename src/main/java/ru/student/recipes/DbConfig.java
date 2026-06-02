package ru.student.recipes;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DbConfig {
    private final String type;
    private final String url;
    private final String user;
    private final String password;

    public DbConfig(String type, String url, String user, String password) {
        this.type = type;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DbConfig load(String path) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(path)) {
            properties.load(input);
        }
        return new DbConfig(
                properties.getProperty("db.type"),
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
