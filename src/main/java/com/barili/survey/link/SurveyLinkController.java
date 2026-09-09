package com.barili.survey.link;

import com.barili.survey.admin.AdminSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SurveyLinkController {
    private final SurveyLinkService service;
    private final AdminSessionService sessionService;
    private final String publicBaseUrl;

    public SurveyLinkController(SurveyLinkService service,
                                AdminSessionService sessionService,
                                @Value("${survey.public-base-url:http://localhost:3000}") String publicBaseUrl) {
        this.service = service;
        this.sessionService = sessionService;
        this.publicBaseUrl = publicBaseUrl;
    }

    @PostMapping("/admin/survey-links")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSurveyLinkResponse create(
            @CookieValue(value = "admin_session", required = false) String sessionToken,
            @Valid @RequestBody CreateSurveyLinkRequest request) {
        sessionService.requireValid(sessionToken);
        SurveyLink link = service.create(request.userGroup(), request.expiresInHours());
        return new CreateSurveyLinkResponse(
                trimTrailingSlash(publicBaseUrl) + "/survey/" + link.getToken(),
                link.getUserGroup(),
                link.getExpiresAt());
    }

    @GetMapping("/survey-links/{token}")
    public SurveyLinkAccessResponse access(@PathVariable String token) {
        SurveyLink link = service.requireAvailable(token);
        return new SurveyLinkAccessResponse(link.getUserGroup(), link.getExpiresAt());
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
