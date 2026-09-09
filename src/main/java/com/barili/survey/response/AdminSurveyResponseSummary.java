package com.barili.survey.response;

import com.barili.survey.question.UserGroup;
import java.time.Instant;
import java.util.UUID;

public record AdminSurveyResponseSummary(
        UUID id,
        UserGroup userGroup,
        String locale,
        Instant submittedAt,
        int answerCount
) {}
