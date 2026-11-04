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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminResponseService {
    private static final int COMMON_ANSWER_LIMIT = 5;

    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public AdminResponseService(SurveyResponseRepository responseRepository,
                                SurveyAnswerRepository answerRepository,
                                QuestionRepository questionRepository,
                                ObjectMapper objectMapper) {
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
    }

    @Cacheable(cacheNames = "adminResponses", key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + (#userGroup == null ? 'ALL' : #userGroup.name())")
    @Transactional(readOnly = true)
    public AdminResponsePage list(Pageable pageable, UserGroup userGroup) {
        var page = userGroup == null
                ? responseRepository.findAllByOrderBySubmittedAtDesc(pageable)
                : responseRepository.findByUserGroupOrderBySubmittedAtDesc(userGroup, pageable);
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

    @Transactional
    @CacheEvict(cacheNames = {"adminResponses", "adminAnalytics", "adminOpenEndedQuestions", "adminOpenEndedResponses"}, allEntries = true)
    public void delete(UUID id) {
        var response = responseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found"));
        responseRepository.delete(response);
    }

    @Cacheable(cacheNames = "adminAnalytics", key = "#userGroup == null ? 'ALL' : #userGroup.name()")
    @Transactional(readOnly = true)
    public AdminSurveyAnalytics analytics(UserGroup userGroup) {
        List<Question> choiceQuestions = questionRepository.findAllByActiveTrueOrderByDisplayNumber().stream()
                .filter(question -> question.getType() != QuestionType.TEXT)
                .filter(question -> userGroup == null || question.getUserGroup() == userGroup)
                .sorted(Comparator.comparing(Question::getUserGroup)
                        .thenComparingInt(Question::getDisplayNumber))
                .toList();
        Map<String, Set<String>> validOptions = choiceQuestions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Question::getCode,
                        question -> question.getOptions().stream()
                                .map(option -> option.getOptionKey())
                                .collect(java.util.stream.Collectors.toCollection(HashSet::new))));

        Map<String, Long> choices = new HashMap<>();
        Map<String, Long> answeredCounts = new HashMap<>();
        for (AdminChoiceAnswerRow answer : answerRepository.findChoiceAnswerRows(userGroup, QuestionType.TEXT)) {
            Set<String> questionOptions = validOptions.get(answer.questionCode());
            if (questionOptions == null) continue;

            List<String> selected = selectedOptions(answer.displayValue()).stream()
                    .filter(questionOptions::contains)
                    .distinct()
                    .toList();
            if (selected.isEmpty()) continue;
            answeredCounts.merge(answer.questionCode(), 1L, Long::sum);
            for (String optionKey : selected) {
                choices.merge(answer.questionCode() + "::" + optionKey, 1L, Long::sum);
            }
        }

        long totalResponses = userGroup == null
                ? responseRepository.count()
                : responseRepository.countByUserGroup(userGroup);
        Map<UserGroup, Long> groups = new EnumMap<>(UserGroup.class);
        for (Object[] row : responseRepository.countGroupedByUserGroup()) {
            groups.put((UserGroup) row[0], ((Number) row[1]).longValue());
        }
        if (userGroup != null) {
            groups.replaceAll((group, count) -> group == userGroup ? count : 0L);
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

        List<AdminQuestionAnalytics> questionAnalytics = choiceQuestions.stream()
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

        return new AdminSurveyAnalytics(totalResponses, groupCounts, topChoices, questionAnalytics);
    }

    @Cacheable(cacheNames = "adminOpenEndedQuestions", key = "#userGroup == null ? 'ALL' : #userGroup.name()")
    @Transactional(readOnly = true)
    public AdminOpenEndedSummary openEndedSummary(UserGroup userGroup) {
        long totalResponses = userGroup == null
                ? responseRepository.count()
                : responseRepository.countByUserGroup(userGroup);
        Map<String, Long> responseCounts = new HashMap<>();
        for (Object[] row : answerRepository.countOpenEndedResponses(userGroup, QuestionType.TEXT)) {
            responseCounts.put((String) row[0], ((Number) row[2]).longValue());
        }

        List<AdminOpenEndedQuestionSummary> questions = questionRepository.findAllByActiveTrueOrderByDisplayNumber().stream()
                .filter(question -> question.getType() == QuestionType.TEXT)
                .filter(question -> userGroup == null || question.getUserGroup() == userGroup)
                .sorted(Comparator.comparing(Question::getUserGroup)
                        .thenComparingInt(Question::getDisplayNumber))
                .map(question -> new AdminOpenEndedQuestionSummary(
                        question.getCode(),
                        question.getUserGroup(),
                        responseCounts.getOrDefault(question.getCode(), 0L),
                        commonAnswers(question.getCode(), userGroup)))
                .toList();
        return new AdminOpenEndedSummary(totalResponses, questions);
    }

    @Cacheable(cacheNames = "adminOpenEndedResponses", key = "#questionCode + ':' + (#userGroup == null ? 'ALL' : #userGroup.name()) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public AdminOpenEndedResponsePage openEndedResponses(String questionCode, UserGroup userGroup, Pageable pageable) {
        Question question = questionRepository.findByCodeAndActiveTrue(questionCode)
                .filter(item -> item.getType() == QuestionType.TEXT)
                .filter(item -> userGroup == null || item.getUserGroup() == userGroup)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open-ended question not found"));
        Page<String> page = answerRepository.findOpenEndedResponses(
                question.getCode(),
                userGroup,
                QuestionType.TEXT,
                pageable);
        List<String> content = page.getContent().stream().map(String::trim).toList();
        return new AdminOpenEndedResponsePage(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
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

    private List<AdminCommonAnswer> commonAnswers(String questionCode, UserGroup userGroup) {
        return answerRepository.findCommonOpenEndedAnswers(
                        questionCode,
                        userGroup,
                        QuestionType.TEXT,
                        PageRequest.of(0, COMMON_ANSWER_LIMIT))
                .stream()
                .map(row -> new AdminCommonAnswer(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();
    }
}
