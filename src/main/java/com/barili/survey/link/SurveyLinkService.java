package com.barili.survey.link;

import com.barili.survey.question.UserGroup;
import java.time.Duration;
import java.time.Instant;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SurveyLinkService {
    private static final int DEFAULT_EXPIRY_HOURS = 72;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SurveyLinkRepository repository;

    public SurveyLinkService(SurveyLinkRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SurveyLink create(UserGroup userGroup, Integer expiresInHours) {
        int expiryHours = expiresInHours == null ? DEFAULT_EXPIRY_HOURS : expiresInHours;
        String token = generateToken();
        return repository.save(new SurveyLink(token, userGroup,
                Instant.now().plus(Duration.ofHours(expiryHours))));
    }

    @Transactional(readOnly = true)
    public SurveyLink requireAvailable(String token) {
        SurveyLink link = repository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey link not found"));
        ensureAvailable(link);
        return link;
    }

    @Transactional
    public SurveyLink consume(String token, UserGroup expectedGroup) {
        SurveyLink link = repository.findByTokenForUpdate(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey link not found"));
        ensureAvailable(link);
        if (link.getUserGroup() != expectedGroup) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey link is for a different questionnaire");
        }
        link.consume(Instant.now());
        return link;
    }

    private void ensureAvailable(SurveyLink link) {
        if (link.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Survey link has already been used");
        }
        if (!link.getExpiresAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Survey link has expired");
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
