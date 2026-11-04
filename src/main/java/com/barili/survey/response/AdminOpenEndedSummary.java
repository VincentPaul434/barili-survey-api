package com.barili.survey.response;

import java.util.List;

public record AdminOpenEndedSummary(
        long totalResponses,
        List<AdminOpenEndedQuestionSummary> questions
) {}
