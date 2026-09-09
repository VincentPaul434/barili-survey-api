package com.barili.survey.question;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "question_options")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String optionKey;
    private int displayOrder;
    private boolean allowsFreeText;

    @ManyToOne
    @JsonIgnore
    private Question question;

    protected QuestionOption() {}

    public QuestionOption(String optionKey, int displayOrder, boolean allowsFreeText) {
        this.optionKey = optionKey;
        this.displayOrder = displayOrder;
        this.allowsFreeText = allowsFreeText;
    }

    void setQuestion(Question question) { this.question = question; }
    public String getOptionKey() { return optionKey; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isAllowsFreeText() { return allowsFreeText; }
}
