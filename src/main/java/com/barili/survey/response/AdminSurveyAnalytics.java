package com.barili.survey.response;

import java.util.List;

public record AdminSurveyAnalytics(
        long totalResponses,
        List<AdminGroupCount> groupCounts,
        List<AdminChoiceCount> topChoices,
        List<AdminQuestionAnalytics> questionAnalytics
) {}
