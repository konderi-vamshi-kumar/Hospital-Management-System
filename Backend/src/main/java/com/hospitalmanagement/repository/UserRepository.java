package com.hospitalmanagement.repository;

import com.hospitalmanagement.model.User;
import com.hospitalmanagement.model.HospitalMembership;
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

    public List<String> findGlobalRolesByUserId(long userId) {
        return jdbcTemplate.queryForList(
                "SELECT role FROM user_roles WHERE user_id = ? ORDER BY role",
                String.class,
                userId
        );
    }

    public List<HospitalMembership> findHospitalMembershipsByUserId(long userId) {
        return jdbcTemplate.query(
                """
                SELECT hospital_id, role, status
                FROM hospital_memberships
                WHERE user_id = ?
                ORDER BY hospital_id, role
                """,
                (resultSet, rowNumber) -> new HospitalMembership(
                        resultSet.getLong("hospital_id"),
                        resultSet.getString("role"),
                        resultSet.getString("status")
                ),
                userId
        );
    }

    public boolean hasActiveHospitalMembership(long userId, long hospitalId) {
        Integer membershipCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM hospital_memberships
                WHERE user_id = ? AND hospital_id = ? AND status = 'ACTIVE'
                """,
                Integer.class,
                userId,
                hospitalId
        );
        return membershipCount != null && membershipCount > 0;
    }

    public void insertGlobalRole(long userId, String role) {
        jdbcTemplate.update(
                "INSERT OR IGNORE INTO user_roles (user_id, role) VALUES (?, ?)",
                userId,
                role
        );
    }

    public void removeGlobalRole(long userId, String role) {
        jdbcTemplate.update(
                "DELETE FROM user_roles WHERE user_id = ? AND role = ?",
                userId,
                role
        );
    }
}
