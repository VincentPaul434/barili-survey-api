package com.barili.survey.admin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAccountService {
    private final AdminAccountRepository repository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AdminAccountService(AdminAccountRepository repository) {
        this.repository = repository;
    }

    public boolean authenticate(String username, String password) {
        return repository.findByUsernameIgnoreCase(username)
                .filter(AdminAccount::isEnabled)
                .map(account -> passwordEncoder.matches(password, account.getPasswordHash()))
                .orElse(false);
    }

    public void createIfMissing(String username, String passwordHash) {
        if (repository.findByUsernameIgnoreCase(username).isEmpty()) {
            repository.save(new AdminAccount(username, passwordHash));
        }
    }
}
