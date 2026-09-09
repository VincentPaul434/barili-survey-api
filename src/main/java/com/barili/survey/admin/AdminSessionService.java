package com.barili.survey.admin;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminSessionService {
    private static final Duration SESSION_TTL = Duration.ofHours(8);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConcurrentHashMap<String, Instant> sessions = new ConcurrentHashMap<>();

    public String create() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, Instant.now().plus(SESSION_TTL));
        return token;
    }

    public void requireValid(String token) {
        if (token == null || token.isBlank()) {
            throw unauthorized();
        }
        Instant expiresAt = sessions.get(token);
        if (expiresAt == null) {
            throw unauthorized();
        }
        if (!expiresAt.isAfter(Instant.now())) {
            sessions.remove(token);
            throw unauthorized();
        }
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }

    public Duration sessionTtl() {
        return SESSION_TTL;
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin login required");
    }
}
