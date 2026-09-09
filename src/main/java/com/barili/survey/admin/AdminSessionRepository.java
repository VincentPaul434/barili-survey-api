package com.barili.survey.admin;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminSessionRepository extends JpaRepository<AdminSession, String> {
    @Modifying
    @Query("delete from AdminSession session where session.expiresAt <= :now")
    int deleteExpired(@Param("now") Instant now);
}
