package com.barili.survey.admin;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminSessionService {
    private static final Duration SESSION_TTL = Duration.ofHours(8);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AdminSessionRepository repository;

    public AdminSessionService(AdminSessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String create() {
        repository.deleteExpired(Instant.now());
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new AdminSession(token, Instant.now().plus(SESSION_TTL)));
        return token;
    }

    @Transactional(readOnly = true)
    public void requireValid(String token) {
        if (token == null || token.isBlank()) throw unauthorized();
        AdminSession session = repository.findById(token).orElseThrow(this::unauthorized);
        if (!session.getExpiresAt().isAfter(Instant.now())) throw unauthorized();
    }

    @Transactional
    public void invalidate(String token) {
        if (token != null && !token.isBlank()) repository.deleteById(token);
    }

    public Duration sessionTtl() {
        return SESSION_TTL;
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin login required");
    }
}
