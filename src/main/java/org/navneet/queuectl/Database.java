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
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA busy_timeout=5000;");
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

        String jobsIndex = """
                CREATE INDEX IF NOT EXISTS idx_jobs_state_next_run
                ON jobs (state, next_run_at);
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(jobsTable);
            statement.execute(configTable);
            statement.execute(jobsIndex);

            // Migrations: add columns needed for logging/timeout bonus features.
            // SQLite has no "ADD COLUMN IF NOT EXISTS", so we check pragma first.
            addColumnIfMissing(connection, "jobs", "log_path", "TEXT");
            addColumnIfMissing(connection, "jobs", "timed_out", "INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(connection, "jobs", "duration_ms", "INTEGER");

        } catch (SQLException e) {
            System.err.println("Database initialisation error: " + e.getMessage());
            throw new RuntimeException("Failed to initialise database", e);
        }
    }

    private static void addColumnIfMissing(Connection connection, String table,
                                           String column, String type) throws SQLException {
        try (Statement st = connection.createStatement()) {
            var rs = st.executeQuery("PRAGMA table_info(" + table + ")");
            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(column)) {
                    return; // already exists
                }
            }
        }
        try (Statement st = connection.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type + ";");
        }
    }
}