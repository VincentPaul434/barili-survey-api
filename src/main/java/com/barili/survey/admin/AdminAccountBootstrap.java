package com.barili.survey.admin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountBootstrap implements CommandLineRunner {
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD_HASH = "$2a$12$EG2IQj0n4VLh3Aqhqcwev.hhmXkL1pOOBky9TYRaJOYTzeOGdq1Ym";

    private final AdminAccountService accountService;

    public AdminAccountBootstrap(AdminAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void run(String... args) {
        accountService.createIfMissing(DEFAULT_USERNAME, DEFAULT_PASSWORD_HASH);
    }
}
