package com.barili.survey.config;

import java.net.URI;
import java.net.HttpURLConnection;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SelfPingScheduler {
    private static final Logger log = LoggerFactory.getLogger(SelfPingScheduler.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final URI healthUri;

    public SelfPingScheduler(@Value("${survey.self-ping-url:}") String selfPingUrl) {
        this.healthUri = healthUri(selfPingUrl);
    }

    @Scheduled(
            fixedDelayString = "${survey.self-ping-interval-ms:600000}",
            initialDelayString = "${survey.self-ping-initial-delay-ms:60000}")
    public void ping() {
        if (healthUri == null) {
            return;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) healthUri.toURL().openConnection();
            connection.setConnectTimeout((int) REQUEST_TIMEOUT.toMillis());
            connection.setReadTimeout((int) REQUEST_TIMEOUT.toMillis());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "barili-survey-self-ping");
            int statusCode = connection.getResponseCode();
            if (statusCode >= 400) {
                log.warn("Self-ping returned HTTP {}", statusCode);
            }
        } catch (Exception exception) {
            log.warn("Self-ping failed: {}", exception.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URI healthUri(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return URI.create(normalizedBaseUrl + "/api/health");
    }
}
