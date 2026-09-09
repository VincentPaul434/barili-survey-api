package com.barili.survey.response;

import java.time.Instant;
import java.util.UUID;

public record SurveySubmissionResult(UUID id, Instant submittedAt, String message) {}
