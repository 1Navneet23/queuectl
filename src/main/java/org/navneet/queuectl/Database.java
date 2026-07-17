package org.navneet.queuectl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String url = "jdbc:sqlite:queuectl.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL;");
            statement.execute("PRAGMA busy_timeout=5000;");
        }

        return connection;
    }

    public static void createTable() {

        String jobsTable = """
                CREATE TABLE IF NOT EXISTS jobs (
                    id TEXT PRIMARY KEY,
                    command TEXT NOT NULL,
                    state TEXT NOT NULL,
                    attempts INTEGER NOT NULL,
                    maxRetries INTEGER NOT NULL,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    next_run_at TEXT,
                    claimed_by TEXT
                );
                """;

        String configTable = """
                CREATE TABLE IF NOT EXISTS config (
                    key TEXT PRIMARY KEY,
                    value TEXT
                );
                """;

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(jobsTable);
            statement.execute(configTable);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}