package com.barili.survey.response;

import com.barili.survey.question.QuestionType;
import com.barili.survey.question.UserGroup;

public record AdminChoiceAnswerRow(
        String questionCode,
        QuestionType questionType,
        UserGroup userGroup,
        String singleValue,
        String multiValueJson
) {
    public String displayValue() {
        return singleValue != null ? singleValue : multiValueJson;
    }
}
