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
                 createdAt, updatedAt, next_run_at, claimed_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert job", e);
        }
    }

    public Optional<Job> findById(String id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find job", e);
        }
    }

    public List<Job> findAll() {
        String sql = "SELECT * FROM jobs";
        List<Job> jobs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                jobs.add(mapRow(rs));
            }
            return jobs;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch jobs", e);
        }
    }

    public List<Job> findByState(JobState state) {
        String sql = "SELECT * FROM jobs WHERE state = ?";
        List<Job> jobs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, state.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapRow(rs));
                }
            }
            return jobs;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch jobs by state", e);
        }
    }

    public void update(Job job) {
        String sql = """
                UPDATE jobs
                SET command = ?,
                    state = ?,
                    attempts = ?,
                    maxRetries = ?,
                    createdAt = ?,
                    updatedAt = ?,
                    next_run_at = ?,
                    claimed_by = ?
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
            statement.setString(9, job.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update job", e);
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
        if (nextRun != null) {
            job.setNextRunAt(Instant.parse(nextRun));
        }

        job.setClaimedBy(rs.getString("claimed_by"));
        return job;
    }
}