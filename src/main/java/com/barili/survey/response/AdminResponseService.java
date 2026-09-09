package com.barili.survey.response;

import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AdminResponseService {
    private final SurveyResponseRepository responseRepository;

    public AdminResponseService(SurveyResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    @Cacheable(cacheNames = "adminResponses", key = "#pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public AdminResponsePage list(Pageable pageable) {
        var page = responseRepository.findAllByOrderBySubmittedAtDesc(pageable);
        var summaries = page.getContent().stream()
                .map(response -> new AdminSurveyResponseSummary(
                        response.getId(),
                        response.getUserGroup(),
                        response.getLocale(),
                        response.getSubmittedAt(),
                        response.getAnswers().size()))
                .toList();
        return new AdminResponsePage(
                summaries,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AdminSurveyResponse details(UUID id) {
        var response = responseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found"));
        return new AdminSurveyResponse(
                response.getId(),
                response.getUserGroup(),
                response.getLocale(),
                response.getSubmittedAt(),
                response.getAnswers().stream()
                        .map(answer -> new AdminSurveyAnswer(
                                answer.getQuestion().getCode(),
                                answer.getDisplayValue(),
                                answer.getOtherValue()))
                        .toList());
    }
}
