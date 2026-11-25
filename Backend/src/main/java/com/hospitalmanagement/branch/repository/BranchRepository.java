package com.hospitalmanagement.branch.repository;

import com.hospitalmanagement.branch.model.Branch;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BranchRepository {

    private static final RowMapper<Branch> BRANCH_ROW_MAPPER = (resultSet, rowNumber) ->
            new Branch(
                    resultSet.getLong("id"),
                    resultSet.getLong("hospital_id"),
                    resultSet.getString("name"),
                    resultSet.getString("address"),
                    resultSet.getString("phone"),
                    resultSet.getString("status"),
                    resultSet.getString("created_at")
            );

    private final JdbcTemplate jdbcTemplate;

    public BranchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Branch> findByHospitalId(long hospitalId) {
        return jdbcTemplate.query(
                """
                SELECT id, hospital_id, name, address, phone, status, created_at
                FROM branches
                WHERE hospital_id = ?
                ORDER BY name COLLATE NOCASE, id
                """,
                BRANCH_ROW_MAPPER,
                hospitalId
        );
    }

    public Optional<Branch> findByIdAndHospitalId(long branchId, long hospitalId) {
        return jdbcTemplate.query(
                """
                SELECT id, hospital_id, name, address, phone, status, created_at
                FROM branches
                WHERE id = ? AND hospital_id = ?
                """,
                BRANCH_ROW_MAPPER,
                branchId,
                hospitalId
        ).stream().findFirst();
    }

    public Optional<Branch> insert(
            long hospitalId,
            String name,
            String address,
            String phone
    ) {
        return jdbcTemplate.query(
                """
                INSERT INTO branches (hospital_id, name, address, phone)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (hospital_id, name) DO NOTHING
                RETURNING id, hospital_id, name, address, phone, status, created_at
                """,
                BRANCH_ROW_MAPPER,
                hospitalId,
                name,
                address,
                phone
        ).stream().findFirst();
    }

    public Optional<Branch> update(
            long branchId,
            long hospitalId,
            String name,
            String address,
            String phone,
            String status
    ) {
        return jdbcTemplate.query(
                """
                UPDATE branches
                SET name = ?, address = ?, phone = ?, status = ?
                WHERE id = ? AND hospital_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM branches existing
                      WHERE existing.hospital_id = ?
                        AND existing.name = ? COLLATE NOCASE
                        AND existing.id != ?
                  )
                RETURNING id, hospital_id, name, address, phone, status, created_at
                """,
                BRANCH_ROW_MAPPER,
                name,
                address,
                phone,
                status,
                branchId,
                hospitalId,
                hospitalId,
                name,
                branchId
        ).stream().findFirst();
    }
}
