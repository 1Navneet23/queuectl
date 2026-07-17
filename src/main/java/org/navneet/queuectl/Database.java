package org.navneet.queuectl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String url = "jdbc:sqlite:queuectl.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
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
            // Create jobs table
            statement.execute(jobsTable);

            // Create config table
            statement.execute(configTable);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}