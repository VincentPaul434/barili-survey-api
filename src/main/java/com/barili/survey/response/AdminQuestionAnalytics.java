package com.barili.survey.response;

import com.barili.survey.question.UserGroup;
import java.util.List;

public record AdminQuestionAnalytics(
        String questionCode,
        UserGroup userGroup,
        long answeredResponses,
        List<AdminChoiceCount> choices
) {}
