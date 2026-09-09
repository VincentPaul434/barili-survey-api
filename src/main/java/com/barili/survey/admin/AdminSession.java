package com.barili.survey.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "admin_sessions")
public class AdminSession {
    @Id
    @Column(length = 64)
    private String token;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    protected AdminSession() {}

    public AdminSession(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
