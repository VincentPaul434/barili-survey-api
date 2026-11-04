create index idx_survey_responses_group_submitted
    on survey_responses(user_group, submitted_at desc);

create index idx_survey_answers_question
    on survey_answers(question_id);
