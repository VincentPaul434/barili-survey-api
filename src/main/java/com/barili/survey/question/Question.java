package com.barili.survey.question;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions", uniqueConstraints = @UniqueConstraint(name = "uk_question_code", columnNames = "code"))
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private int displayNumber;

    @Enumerated(EnumType.STRING)
    private UserGroup userGroup;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private String promptKey;
    private boolean required;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuestionOption> options = new ArrayList<>();

    protected Question() {}

    public Question(String code, int displayNumber, UserGroup userGroup, QuestionType type,
                    String promptKey, boolean required) {
        this.code = code;
        this.displayNumber = displayNumber;
        this.userGroup = userGroup;
        this.type = type;
        this.promptKey = promptKey;
        this.required = required;
    }

    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public int getDisplayNumber() { return displayNumber; }
    public UserGroup getUserGroup() { return userGroup; }
    public QuestionType getType() { return type; }
    public String getPromptKey() { return promptKey; }
    public boolean isRequired() { return required; }
    public List<QuestionOption> getOptions() { return options; }
}
