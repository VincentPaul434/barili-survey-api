package com.barili.survey.response;

import java.util.List;

public record AdminResponsePage(
        List<AdminSurveyResponseSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
