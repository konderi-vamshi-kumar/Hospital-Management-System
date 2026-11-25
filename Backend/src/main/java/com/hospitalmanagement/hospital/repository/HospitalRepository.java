package com.hospitalmanagement.hospital.repository;

import com.hospitalmanagement.hospital.model.Hospital;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalRepository {

    private static final RowMapper<Hospital> HOSPITAL_ROW_MAPPER = (resultSet, rowNumber) ->
            new Hospital(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("status"),
                    resultSet.getString("created_at")
            );

    private final JdbcTemplate jdbcTemplate;

    public HospitalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Hospital> findAll() {
        return jdbcTemplate.query(
                """
                SELECT id, name, status, created_at
                FROM hospitals
                ORDER BY name COLLATE NOCASE, id
                """,
                HOSPITAL_ROW_MAPPER
        );
    }

    public Optional<Hospital> findById(long hospitalId) {
        return jdbcTemplate.query(
                """
                SELECT id, name, status, created_at
                FROM hospitals
                WHERE id = ?
                """,
                HOSPITAL_ROW_MAPPER,
                hospitalId
        ).stream().findFirst();
    }

    public Hospital insert(String name) {
        return jdbcTemplate.query(
                """
                INSERT INTO hospitals (name)
                VALUES (?)
                RETURNING id, name, status, created_at
                """,
                HOSPITAL_ROW_MAPPER,
                name
        ).stream().findFirst().orElseThrow(() ->
                new IllegalStateException("The new hospital could not be loaded."));
    }

    public Optional<Hospital> update(long hospitalId, String name, String status) {
        return jdbcTemplate.query(
                """
                UPDATE hospitals
                SET name = ?, status = ?
                WHERE id = ?
                RETURNING id, name, status, created_at
                """,
                HOSPITAL_ROW_MAPPER,
                name,
                status,
                hospitalId
        ).stream().findFirst();
    }
}
