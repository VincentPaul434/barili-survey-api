package com.barili.survey.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountBootstrap implements CommandLineRunner {
    private final AdminAccountService accountService;
    private final String username;
    private final String passwordHash;

    public AdminAccountBootstrap(
            AdminAccountService accountService,
            @Value("${survey.admin-username:}") String username,
            @Value("${survey.admin-password-hash:}") String passwordHash) {
        this.accountService = accountService;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    @Override
    public void run(String... args) {
        if (username.isBlank() && passwordHash.isBlank()) return;
        if (username.isBlank() || passwordHash.isBlank()) {
            throw new IllegalStateException("SURVEY_ADMIN_USERNAME and SURVEY_ADMIN_PASSWORD_HASH must be set together");
        }
        accountService.createIfMissing(username, passwordHash);
    }
}
