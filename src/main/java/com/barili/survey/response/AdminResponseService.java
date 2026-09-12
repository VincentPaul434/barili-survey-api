package com.barili.survey.response;

import com.barili.survey.question.Question;
import com.barili.survey.question.QuestionRepository;
import com.barili.survey.question.QuestionType;
import com.barili.survey.question.UserGroup;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public AdminResponseService(SurveyResponseRepository responseRepository,
                                QuestionRepository questionRepository,
                                ObjectMapper objectMapper) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
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

    @Cacheable(cacheNames = "adminAnalytics", key = "#userGroup == null ? 'ALL' : #userGroup.name()")
    @Transactional(readOnly = true)
    public AdminSurveyAnalytics analytics(UserGroup userGroup) {
        List<SurveyResponse> responses = responseRepository.findAll().stream()
                .filter(response -> userGroup == null || response.getUserGroup() == userGroup)
                .toList();
        Map<UserGroup, Long> groups = new EnumMap<>(UserGroup.class);
        Map<String, Long> choices = new HashMap<>();
        Map<String, Long> answeredCounts = new HashMap<>();

        for (SurveyResponse response : responses) {
            groups.merge(response.getUserGroup(), 1L, Long::sum);
            for (SurveyAnswer answer : response.getAnswers()) {
                if (answer.getQuestion().getType() == QuestionType.TEXT) {
                    continue;
                }
                answeredCounts.merge(answer.getQuestion().getCode(), 1L, Long::sum);
                for (String optionKey : selectedOptions(answer.getDisplayValue())) {
                    choices.merge(answer.getQuestion().getCode() + "::" + optionKey, 1L, Long::sum);
                }
            }
        }

        List<AdminGroupCount> groupCounts = Arrays.stream(UserGroup.values())
                .map(group -> new AdminGroupCount(group, groups.getOrDefault(group, 0L)))
                .toList();
        List<AdminChoiceCount> topChoices = choices.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("::", 2);
                    return new AdminChoiceCount(parts[0], parts[1], entry.getValue());
                })
                .sorted(Comparator.comparingLong(AdminChoiceCount::count).reversed()
                        .thenComparing(AdminChoiceCount::questionCode)
                        .thenComparing(AdminChoiceCount::optionKey))
                .limit(8)
                .toList();

        List<AdminQuestionAnalytics> questionAnalytics = questionRepository.findAll().stream()
                .filter(question -> question.getType() != QuestionType.TEXT)
                .filter(question -> userGroup == null || question.getUserGroup() == userGroup)
                .sorted(Comparator.comparing(Question::getUserGroup)
                        .thenComparingInt(Question::getDisplayNumber))
                .map(question -> new AdminQuestionAnalytics(
                        question.getCode(),
                        question.getUserGroup(),
                        answeredCounts.getOrDefault(question.getCode(), 0L),
                        question.getOptions().stream()
                                .sorted(Comparator.comparingInt(option -> option.getDisplayOrder()))
                                .map(option -> new AdminChoiceCount(
                                        question.getCode(),
                                        option.getOptionKey(),
                                        choices.getOrDefault(question.getCode() + "::" + option.getOptionKey(), 0L)))
                                .toList()))
                .toList();

        return new AdminSurveyAnalytics(responses.size(), groupCounts, topChoices, questionAnalytics);
    }

    private List<String> selectedOptions(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            var parsed = objectMapper.readTree(value);
            if (parsed.isArray()) {
                var options = new java.util.ArrayList<String>();
                parsed.forEach(node -> options.add(node.asText()));
                return options;
            }
        } catch (Exception ignored) {
            // Single-choice answers are stored as plain option keys.
        }
        return List.of(value);
    }
}
