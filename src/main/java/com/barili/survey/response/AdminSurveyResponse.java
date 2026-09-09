package com.barili.survey.response;

import com.barili.survey.question.UserGroup;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminSurveyResponse(
        UUID id,
        UserGroup userGroup,
        String locale,
        Instant submittedAt,
        List<AdminSurveyAnswer> answers
) {}
