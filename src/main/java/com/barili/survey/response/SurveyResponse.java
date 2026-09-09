package com.barili.survey.response;

import com.barili.survey.question.UserGroup;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "survey_responses")
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private UserGroup userGroup;

    private String locale;
    private Instant submittedAt;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswer> answers = new ArrayList<>();

    protected SurveyResponse() {}

    public SurveyResponse(UserGroup userGroup, String locale) {
        this.userGroup = userGroup;
        this.locale = locale;
    }

    @PrePersist
    void setSubmittedAt() {
        if (submittedAt == null) submittedAt = Instant.now();
    }

    public void addAnswer(SurveyAnswer answer) {
        answers.add(answer);
        answer.setResponse(this);
    }

    public UUID getId() { return id; }
    public UserGroup getUserGroup() { return userGroup; }
    public String getLocale() { return locale; }
    public Instant getSubmittedAt() { return submittedAt; }
    public List<SurveyAnswer> getAnswers() { return answers; }
}
