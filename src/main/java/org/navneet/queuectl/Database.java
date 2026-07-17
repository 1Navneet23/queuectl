package org.navneet.queuectl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:queuectl.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement st = connection.createStatement()) {
            // WAL must be set before busy_timeout for correct effect order
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA busy_timeout=5000;");
            // Reduce fsync overhead while maintaining crash safety in WAL mode
            st.execute("PRAGMA synchronous=NORMAL;");
        }
        return connection;
    }

    public static void createTable() {

        String jobsTable = """
                CREATE TABLE IF NOT EXISTS jobs (
                    id          TEXT PRIMARY KEY,
                    command     TEXT NOT NULL,
                    state       TEXT NOT NULL,
                    attempts    INTEGER NOT NULL DEFAULT 0,
                    maxRetries  INTEGER NOT NULL DEFAULT 3,
                    createdAt   TEXT NOT NULL,
                    updatedAt   TEXT NOT NULL,
                    next_run_at TEXT,
                    claimed_by  TEXT
                );
                """;

        String configTable = """
                CREATE TABLE IF NOT EXISTS config (
                    key   TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
                """;

        // Index to speed up claimNextJob — queries filter on state + next_run_at
        String jobsIndex = """
                CREATE INDEX IF NOT EXISTS idx_jobs_state_next_run
                ON jobs (state, next_run_at);
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(jobsTable);
            statement.execute(configTable);
            statement.execute(jobsIndex);

        } catch (SQLException e) {
            System.err.println("Database initialisation error: " + e.getMessage());
            throw new RuntimeException("Failed to initialise database", e);
        }
    }
}