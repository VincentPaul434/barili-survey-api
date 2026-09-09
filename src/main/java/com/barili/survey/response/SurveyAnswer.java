package com.barili.survey.response;

import com.barili.survey.question.Question;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "survey_answers", uniqueConstraints = @UniqueConstraint(
        name = "uk_response_question", columnNames = {"response_id", "question_id"}))
public class SurveyAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private SurveyResponse response;

    @ManyToOne(optional = false)
    private Question question;

    private String singleValue;
    private String multiValueJson;
    private String textValue;
    private String otherValue;

    protected SurveyAnswer() {}

    public SurveyAnswer(Question question, String singleValue, String multiValueJson,
                        String textValue, String otherValue) {
        this.question = question;
        this.singleValue = singleValue;
        this.multiValueJson = multiValueJson;
        this.textValue = textValue;
        this.otherValue = otherValue;
    }

    void setResponse(SurveyResponse response) { this.response = response; }
}
