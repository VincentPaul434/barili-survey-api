package com.barili.survey.response;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {
    java.util.List<SurveyResponse> findAllByOrderBySubmittedAtDesc();
}
