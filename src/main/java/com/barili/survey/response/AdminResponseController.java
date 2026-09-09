package com.barili.survey.response;

import com.barili.survey.admin.AdminSessionService;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/responses")
public class AdminResponseController {
    private static final String SESSION_COOKIE = "admin_session";

    private final SurveyResponseRepository responseRepository;
    private final AdminSessionService sessionService;

    public AdminResponseController(SurveyResponseRepository responseRepository,
                                   AdminSessionService sessionService) {
        this.responseRepository = responseRepository;
        this.sessionService = sessionService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<AdminSurveyResponse> list(
            @CookieValue(value = SESSION_COOKIE, required = false) String sessionToken) {
        sessionService.requireValid(sessionToken);
        return responseRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(response -> new AdminSurveyResponse(
                        response.getId(),
                        response.getUserGroup(),
                        response.getLocale(),
                        response.getSubmittedAt(),
                        response.getAnswers().stream()
                                .map(answer -> new AdminSurveyAnswer(
                                        answer.getQuestion().getCode(),
                                        answer.getDisplayValue(),
                                        answer.getOtherValue()))
                                .toList()))
                .toList();
    }
}
