package com.barili.survey.response;

import com.barili.survey.question.UserGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record SurveySubmission(
        @NotNull UserGroup userGroup,
        @NotBlank String locale,
        @NotNull Map<String, Object> answers,
        Map<String, String> otherAnswers
) {
    public Map<String, String> safeOtherAnswers() {
        return otherAnswers == null ? Map.of() : otherAnswers;
    }

    public static List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return value == null ? List.of() : List.of(String.valueOf(value));
    }
}
