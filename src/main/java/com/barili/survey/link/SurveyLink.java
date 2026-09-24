package com.barili.survey.link;

import com.barili.survey.question.UserGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "survey_links")
public class SurveyLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_group", nullable = false)
    private UserGroup userGroup;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean reusable;

    private Instant consumedAt;

    protected SurveyLink() {}

    public SurveyLink(String token, UserGroup userGroup, Instant expiresAt) {
        this(token, userGroup, expiresAt, false);
    }

    public SurveyLink(String token, UserGroup userGroup, Instant expiresAt, boolean reusable) {
        this.token = token;
        this.userGroup = userGroup;
        this.expiresAt = expiresAt;
        this.reusable = reusable;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public boolean isAvailable(Instant now) {
        return (reusable || consumedAt == null) && expiresAt.isAfter(now);
    }

    public void consume(Instant now) {
        if (!reusable) consumedAt = now;
    }

    public String getToken() { return token; }
    public UserGroup getUserGroup() { return userGroup; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isReusable() { return reusable; }
    public Instant getConsumedAt() { return consumedAt; }
}
