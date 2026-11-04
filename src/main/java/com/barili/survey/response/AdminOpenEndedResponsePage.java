package com.barili.survey.response;

import java.util.List;

public record AdminOpenEndedResponsePage(
        List<String> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
