package com.barili.survey.response;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {
    Page<SurveyResponse> findAllByOrderBySubmittedAtDesc(Pageable pageable);
}
