package com.barili.survey.link;

import com.barili.survey.question.UserGroup;
import java.time.Instant;

public record CreateSurveyLinkResponse(String link, UserGroup userGroup, Instant expiresAt) {}
