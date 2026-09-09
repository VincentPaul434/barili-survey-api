package com.barili.survey.link;

import com.barili.survey.question.UserGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSurveyLinkRequest(
        @NotNull UserGroup userGroup,
        @Min(1) @Max(720) Integer expiresInHours
) {}
