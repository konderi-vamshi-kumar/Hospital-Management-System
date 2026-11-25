package com.hospitalmanagement.department.repository;

import com.hospitalmanagement.department.model.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentRepository {

    private static final RowMapper<Department> DEPARTMENT_ROW_MAPPER =
            (resultSet, rowNumber) -> new Department(
                    resultSet.getLong("id"),
                    resultSet.getLong("hospital_id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("status"),
                    resultSet.getString("created_at")
            );

    private final JdbcTemplate jdbcTemplate;

    public DepartmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Department> findByHospitalId(long hospitalId) {
        return jdbcTemplate.query(
                """
                SELECT id, hospital_id, name, description, status, created_at
                FROM departments
                WHERE hospital_id = ?
                ORDER BY name COLLATE NOCASE, id
                """,
                DEPARTMENT_ROW_MAPPER,
                hospitalId
        );
    }

    public Optional<Department> findByIdAndHospitalId(long departmentId, long hospitalId) {
        return jdbcTemplate.query(
                """
                SELECT id, hospital_id, name, description, status, created_at
                FROM departments
                WHERE id = ? AND hospital_id = ?
                """,
                DEPARTMENT_ROW_MAPPER,
                departmentId,
                hospitalId
        ).stream().findFirst();
    }

    public Optional<Department> insert(long hospitalId, String name, String description) {
        return jdbcTemplate.query(
                """
                INSERT INTO departments (hospital_id, name, description)
                VALUES (?, ?, ?)
                ON CONFLICT (hospital_id, name) DO NOTHING
                RETURNING id, hospital_id, name, description, status, created_at
                """,
                DEPARTMENT_ROW_MAPPER,
                hospitalId,
                name,
                description
        ).stream().findFirst();
    }

    public Optional<Department> update(
            long departmentId,
            long hospitalId,
            String name,
            String description,
            String status
    ) {
        return jdbcTemplate.query(
                """
                UPDATE departments
                SET name = ?, description = ?, status = ?
                WHERE id = ? AND hospital_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM departments existing
                      WHERE existing.hospital_id = ?
                        AND existing.name = ? COLLATE NOCASE
                        AND existing.id != ?
                  )
                RETURNING id, hospital_id, name, description, status, created_at
                """,
                DEPARTMENT_ROW_MAPPER,
                name,
                description,
                status,
                departmentId,
                hospitalId,
                hospitalId,
                name,
                departmentId
        ).stream().findFirst();
    }
}
