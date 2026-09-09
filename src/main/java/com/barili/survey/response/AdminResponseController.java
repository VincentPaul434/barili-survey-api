package com.barili.survey.response;

import com.barili.survey.admin.AdminSessionService;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/responses")
public class AdminResponseController {
    private static final String SESSION_COOKIE = "admin_session";

    private final AdminSessionService sessionService;
    private final AdminResponseService responseService;

    public AdminResponseController(AdminSessionService sessionService,
                                   AdminResponseService responseService) {
        this.sessionService = sessionService;
        this.responseService = responseService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public AdminResponsePage list(
            @CookieValue(value = SESSION_COOKIE, required = false) String sessionToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        sessionService.requireValid(sessionToken);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return responseService.list(PageRequest.of(safePage, safeSize));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public AdminSurveyResponse details(
            @CookieValue(value = SESSION_COOKIE, required = false) String sessionToken,
            @PathVariable UUID id) {
        sessionService.requireValid(sessionToken);
        return responseService.details(id);
    }
}
