package org.navneet.queuectl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobRepository {
    public void insert(Job job) {
        String sql = """
                INSERT INTO jobs
                (id, command, state, attempts, maxRetries,
                 createdAt, updatedAt, next_run_at, claimed_by,
                 log_path, timed_out, duration_ms)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, job.getId());
            statement.setString(2, job.getCommand());
            statement.setString(3, job.getState().name());
            statement.setInt(4, job.getAttempts());
            statement.setInt(5, job.getMaxRetries());
            statement.setString(6, job.getCreatedAt().toString());
            statement.setString(7, job.getUpdatedAt().toString());
            setNullableString(statement, 8, job.getNextRunAt() != null ? job.getNextRunAt().toString() : null);
            setNullableString(statement, 9, job.getClaimedBy());
            setNullableString(statement, 10, job.getLogPath());
            statement.setInt(11, job.isTimedOut() ? 1 : 0);
            if (job.getDurationMs() != null) {
                statement.setLong(12, job.getDurationMs());
            } else {
                statement.setNull(12, Types.BIGINT);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert job", e);
        }
    }

    public void update(Job job) {
        String sql = """
                UPDATE jobs
                SET command = ?, state = ?, attempts = ?, maxRetries = ?,
                    createdAt = ?, updatedAt = ?, next_run_at = ?, claimed_by = ?,
                    log_path = ?, timed_out = ?, duration_ms = ?
                WHERE id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, job.getCommand());
            statement.setString(2, job.getState().name());
            statement.setInt(3, job.getAttempts());
            statement.setInt(4, job.getMaxRetries());
            statement.setString(5, job.getCreatedAt().toString());
            statement.setString(6, job.getUpdatedAt().toString());
            setNullableString(statement, 7, job.getNextRunAt() != null ? job.getNextRunAt().toString() : null);
            setNullableString(statement, 8, job.getClaimedBy());
            setNullableString(statement, 9, job.getLogPath());
            statement.setInt(10, job.isTimedOut() ? 1 : 0);
            if (job.getDurationMs() != null) {
                statement.setLong(11, job.getDurationMs());
            } else {
                statement.setNull(11, Types.BIGINT);
            }
            statement.setString(12, job.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update job", e);
        }
    }


    public Optional<Job> findById(String id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find job", e);
        }
    }

    public List<Job> findAll() {
        String sql = "SELECT * FROM jobs ORDER BY createdAt";
        List<Job> jobs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) jobs.add(mapRow(rs));
            return jobs;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch jobs", e);
        }
    }

    public List<Job> findByState(JobState state) {
        String sql = "SELECT * FROM jobs WHERE state = ? ORDER BY createdAt";
        List<Job> jobs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, state.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }
            return jobs;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch jobs by state", e);
        }
    }


    public Optional<Job> claimNextJob(String workerId) {
        String sql = """
                UPDATE jobs
                SET state = ?, claimed_by = ?, updatedAt = ?
                WHERE id = (
                    SELECT id FROM jobs
                    WHERE (
                            (state = ? AND (next_run_at IS NULL OR next_run_at <= ?))
                         OR (state = ? AND next_run_at <= ?)
                    )
                    ORDER BY createdAt
                    LIMIT 1
                )
                RETURNING *
                """;

        String now = Instant.now().toString();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, JobState.PROCESSING.name());
            statement.setString(2, workerId);
            statement.setString(3, now);
            statement.setString(4, JobState.PENDING.name());
            statement.setString(5, now);
            statement.setString(6, JobState.FAILED.name());
            statement.setString(7, now);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to claim job", e);
        }
    }

    /**
     * On worker startup, release any PROCESSING jobs previously claimed by this
     * worker ID (left over from a crash). Resets them to PENDING so they can be
     * retried.
     */
    public void releaseStaleJobs(String workerId) {
        String sql = """
                UPDATE jobs
                SET state = ?, claimed_by = NULL, next_run_at = NULL, updatedAt = ?
                WHERE state = ? AND claimed_by = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, JobState.PENDING.name());
            statement.setString(2, Instant.now().toString());
            statement.setString(3, JobState.PROCESSING.name());
            statement.setString(4, workerId);

            int released = statement.executeUpdate();
            if (released > 0) {
                System.out.println("Recovered " + released + " stale PROCESSING job(s) from previous run.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to release stale jobs", e);
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value != null) {
            statement.setString(index, value);
        } else {
            statement.setNull(index, Types.VARCHAR);
        }
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getString("id"));
        job.setCommand(rs.getString("command"));
        job.setState(JobState.valueOf(rs.getString("state")));
        job.setAttempts(rs.getInt("attempts"));
        job.setMaxRetries(rs.getInt("maxRetries"));
        job.setCreatedAt(Instant.parse(rs.getString("createdAt")));
        job.setUpdatedAt(Instant.parse(rs.getString("updatedAt")));

        String nextRun = rs.getString("next_run_at");
        if (nextRun != null) job.setNextRunAt(Instant.parse(nextRun));

        job.setClaimedBy(rs.getString("claimed_by"));
        job.setLogPath(rs.getString("log_path"));
        job.setTimedOut(rs.getInt("timed_out") == 1);
        long duration = rs.getLong("duration_ms");
        job.setDurationMs(rs.wasNull() ? null : duration);
        return job;
    }
}