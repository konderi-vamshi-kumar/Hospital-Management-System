package com.studentmanagement.repository;

import com.studentmanagement.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByUsername(String username) {
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ? COLLATE NOCASE",
                Integer.class,
                username
        );

        return userCount != null && userCount > 0;
    }

    public void insert(String username, String passwordHash) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password_hash) VALUES (?, ?)",
                username,
                passwordHash
        );
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = jdbcTemplate.query(
                """
                SELECT id, username, password_hash, created_at
                FROM users
                WHERE username = ? COLLATE NOCASE
                """,
                (resultSet, rowNumber) -> new User(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("created_at")
                ),
                username
        );

        return users.stream().findFirst();
    }
}
