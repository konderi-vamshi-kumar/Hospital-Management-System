package com.hospitalmanagement.membership.repository;

import com.hospitalmanagement.membership.model.StaffInvitation;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StaffInvitationRepository {

    private static final String INVITATION_COLUMNS = """
            id,
            hospital_id,
            role,
            CASE
                WHEN status = 'PENDING'
                     AND expires_at <= strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
                THEN 'EXPIRED'
                ELSE status
            END AS status,
            created_at,
            expires_at
            """;

    private static final RowMapper<StaffInvitation> INVITATION_ROW_MAPPER =
            (resultSet, rowNumber) -> new StaffInvitation(
                    resultSet.getLong("id"),
                    resultSet.getLong("hospital_id"),
                    resultSet.getString("role"),
                    resultSet.getString("status"),
                    resultSet.getString("created_at"),
                    resultSet.getString("expires_at")
            );

    private final JdbcTemplate jdbcTemplate;

    public StaffInvitationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public StaffInvitation create(
            long hospitalId,
            String role,
            String tokenHash,
            long invitedByUserId
    ) {
        return jdbcTemplate.query(
                """
                INSERT INTO staff_invitations (
                    hospital_id, role, token_hash, invited_by_user_id, expires_at
                )
                VALUES (
                    ?, ?, ?, ?,
                    strftime('%Y-%m-%dT%H:%M:%fZ', 'now', '+7 days')
                )
                RETURNING id, hospital_id, role, status, created_at, expires_at
                """,
                INVITATION_ROW_MAPPER,
                hospitalId,
                role,
                tokenHash,
                invitedByUserId
        ).stream().findFirst().orElseThrow(() ->
                new IllegalStateException("The staff invitation could not be created."));
    }

    public List<StaffInvitation> findByHospitalId(long hospitalId) {
        return jdbcTemplate.query(
                "SELECT " + INVITATION_COLUMNS + """
                FROM staff_invitations
                WHERE hospital_id = ?
                ORDER BY created_at DESC, id DESC
                """,
                INVITATION_ROW_MAPPER,
                hospitalId
        );
    }

    public Optional<StaffInvitation> findByTokenHash(String tokenHash) {
        return jdbcTemplate.query(
                "SELECT " + INVITATION_COLUMNS + """
                FROM staff_invitations
                WHERE token_hash = ?
                """,
                INVITATION_ROW_MAPPER,
                tokenHash
        ).stream().findFirst();
    }

    public Optional<StaffInvitation> findByIdAndHospitalId(
            long invitationId,
            long hospitalId
    ) {
        return jdbcTemplate.query(
                "SELECT " + INVITATION_COLUMNS + """
                FROM staff_invitations
                WHERE id = ? AND hospital_id = ?
                """,
                INVITATION_ROW_MAPPER,
                invitationId,
                hospitalId
        ).stream().findFirst();
    }

    public boolean markAccepted(long invitationId, long acceptedByUserId) {
        return jdbcTemplate.update(
                """
                UPDATE staff_invitations
                SET status = 'ACCEPTED',
                    accepted_by_user_id = ?,
                    accepted_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
                WHERE id = ?
                  AND status = 'PENDING'
                  AND expires_at > strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
                """,
                acceptedByUserId,
                invitationId
        ) == 1;
    }

    public boolean revoke(long invitationId, long hospitalId) {
        return jdbcTemplate.update(
                """
                UPDATE staff_invitations
                SET status = 'REVOKED'
                WHERE id = ?
                  AND hospital_id = ?
                  AND status = 'PENDING'
                  AND expires_at > strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
                """,
                invitationId,
                hospitalId
        ) == 1;
    }
}
