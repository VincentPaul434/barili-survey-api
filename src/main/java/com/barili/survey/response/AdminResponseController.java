package com.barili.survey.response;

import com.barili.survey.admin.AdminSessionService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(
            @CookieValue(value = SESSION_COOKIE, required = false) String sessionToken,
            @PathVariable UUID id) {
        sessionService.requireValid(sessionToken);
        var response = responseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found"));
        responseRepository.delete(response);
    }
}
