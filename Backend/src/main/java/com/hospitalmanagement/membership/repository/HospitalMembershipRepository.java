package com.hospitalmanagement.membership.repository;

import com.hospitalmanagement.membership.model.HospitalAdministrator;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalMembershipRepository {

    private static final RowMapper<HospitalAdministrator> ADMIN_ROW_MAPPER =
            (resultSet, rowNumber) -> new HospitalAdministrator(
                    resultSet.getLong("membership_id"),
                    resultSet.getLong("user_id"),
                    resultSet.getLong("hospital_id"),
                    resultSet.getString("username"),
                    resultSet.getString("status"),
                    resultSet.getString("created_at")
            );

    private final JdbcTemplate jdbcTemplate;

    public HospitalMembershipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<HospitalAdministrator> findAdministratorsByHospitalId(long hospitalId) {
        return jdbcTemplate.query(
                """
                SELECT memberships.id AS membership_id,
                       users.id AS user_id,
                       memberships.hospital_id,
                       users.username,
                       memberships.status,
                       memberships.created_at
                FROM hospital_memberships memberships
                JOIN users ON users.id = memberships.user_id
                WHERE memberships.hospital_id = ?
                  AND memberships.role = 'HOSPITAL_ADMIN'
                ORDER BY users.username COLLATE NOCASE, users.id
                """,
                ADMIN_ROW_MAPPER,
                hospitalId
        );
    }

    public HospitalAdministrator assignAdministrator(
            long userId,
            long hospitalId,
            String username
    ) {
        return jdbcTemplate.query(
                """
                INSERT INTO hospital_memberships (user_id, hospital_id, role, status)
                VALUES (?, ?, 'HOSPITAL_ADMIN', 'ACTIVE')
                ON CONFLICT (user_id, hospital_id, role)
                DO UPDATE SET status = 'ACTIVE'
                RETURNING id AS membership_id,
                          user_id,
                          hospital_id,
                          status,
                          created_at
                """,
                (resultSet, rowNumber) -> new HospitalAdministrator(
                        resultSet.getLong("membership_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getLong("hospital_id"),
                        username,
                        resultSet.getString("status"),
                        resultSet.getString("created_at")
                ),
                userId,
                hospitalId
        ).stream().findFirst().orElseThrow(() ->
                new IllegalStateException("The hospital administrator assignment could not be loaded."));
    }
}
