package com.barili.survey.response;

import com.barili.survey.question.UserGroup;

public record AdminGroupCount(UserGroup userGroup, long count) {}
