package com.barili.survey.link;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyLinkRepository extends JpaRepository<SurveyLink, UUID> {
    Optional<SurveyLink> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select link from SurveyLink link where link.token = :token")
    Optional<SurveyLink> findByTokenForUpdate(@Param("token") String token);
}
