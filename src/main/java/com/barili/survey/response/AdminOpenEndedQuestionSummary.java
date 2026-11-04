package com.barili.survey.response;

import com.barili.survey.question.UserGroup;

public record AdminOpenEndedQuestionSummary(
        String questionCode,
        UserGroup userGroup,
        long answeredResponses
) {}
