package com.barili.survey.admin;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {
    private static final String COOKIE_NAME = "admin_session";

    private final AdminSessionService sessionService;
    private final AdminAccountService accountService;
    private final boolean secureCookie;
    private final String sameSite;

    public AdminAuthController(
            AdminSessionService sessionService,
            AdminAccountService accountService,
            @Value("${survey.admin-cookie-secure:true}") boolean secureCookie,
            @Value("${survey.admin-cookie-same-site:Lax}") String sameSite) {
        this.sessionService = sessionService;
        this.accountService = accountService;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request,
                                    HttpServletResponse response) {
        if (!accountService.authenticate(request.username(), request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
        }

        String sessionToken = sessionService.create();
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(sessionToken).toString());
        return new AdminLoginResponse("Admin login successful");
    }

    @GetMapping("/session")
    public AdminLoginResponse session(@CookieValue(value = COOKIE_NAME, required = false) String sessionToken) {
        sessionService.requireValid(sessionToken);
        return new AdminLoginResponse("Admin session is active");
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(value = COOKIE_NAME, required = false) String sessionToken,
                       HttpServletResponse response) {
        sessionService.invalidate(sessionToken);
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie().toString());
    }

    private ResponseCookie sessionCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api/admin")
                .maxAge(sessionService.sessionTtl())
                .build();
    }

    private ResponseCookie expiredCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api/admin")
                .maxAge(0)
                .build();
    }

}
