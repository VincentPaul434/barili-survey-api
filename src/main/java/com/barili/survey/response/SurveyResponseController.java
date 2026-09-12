package com.barili.survey.response;

import com.barili.survey.link.SurveyLinkService;
import com.barili.survey.question.Question;
import com.barili.survey.question.QuestionRepository;
import com.barili.survey.question.QuestionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

@RestController
@RequestMapping("/api/surveys")
public class SurveyResponseController {
    private final SurveyResponseRepository responseRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final SurveyLinkService surveyLinkService;

    public SurveyResponseController(SurveyResponseRepository responseRepository,
                                    QuestionRepository questionRepository,
                                    ObjectMapper objectMapper,
                                    SurveyLinkService surveyLinkService) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
        this.surveyLinkService = surveyLinkService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @CacheEvict(cacheNames = {"adminResponses", "adminAnalytics"}, allEntries = true)
    public SurveySubmissionResult submit(@Valid @RequestBody SurveySubmission submission) {
        var link = surveyLinkService.requireAvailable(submission.linkToken());
        if (link.getUserGroup() != submission.userGroup()) {
            throw badRequest("Survey link is for a different questionnaire");
        }

        if (!Set.of("en", "ceb").contains(submission.locale())) {
            throw badRequest("Unsupported locale");
        }

        List<Question> questions = questionRepository
                .findAllByUserGroupOrderByDisplayNumber(submission.userGroup());
        validateAnswers(submission, questions);
        surveyLinkService.consume(submission.linkToken(), submission.userGroup());

        SurveyResponse response = new SurveyResponse(submission.userGroup(), submission.locale());
        Map<String, Object> answers = submission.answers();

        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            Question question = questionRepository.findByCode(entry.getKey()).orElseThrow(() -> badRequest(
                    "Unknown question code: " + entry.getKey()));
            Object value = entry.getValue();
            String single = null;
            String multi = null;
            String text = null;
            if (question.getType() == QuestionType.MULTI) {
                try {
                    multi = objectMapper.writeValueAsString(SurveySubmission.asStringList(value));
                } catch (JsonProcessingException ex) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid multi-answer", ex);
                }
            } else if (question.getType() == QuestionType.TEXT) {
                text = value == null ? null : String.valueOf(value);
            } else {
                single = value == null ? null : String.valueOf(value);
            }
            response.addAnswer(new SurveyAnswer(question, single, multi, text,
                    submission.safeOtherAnswers().get(entry.getKey())));
        }

        SurveyResponse saved = responseRepository.save(response);
        return new SurveySubmissionResult(saved.getId(), saved.getSubmittedAt(), "Survey response saved");
    }

    private void validateAnswers(SurveySubmission submission, List<Question> questions) {
        Map<String, Object> answers = submission.answers();
        Set<String> questionCodes = questions.stream().map(Question::getCode).collect(Collectors.toSet());
        for (String code : answers.keySet()) {
            if (!questionCodes.contains(code)) throw badRequest("Unknown question code: " + code);
        }

        for (Question question : questions) {
            if (!answers.containsKey(question.getCode())) {
                if (question.isRequired()) throw badRequest("Missing required answer: " + question.getCode());
                continue;
            }
            Object value = answers.get(question.getCode());
            Set<String> optionKeys = question.getOptions().stream()
                    .map(option -> option.getOptionKey())
                    .collect(Collectors.toSet());
            Set<String> selected = new HashSet<>();

            if (question.getType() == QuestionType.MULTI) {
                if (!(value instanceof List<?> values) || values.isEmpty()) {
                    throw badRequest("Invalid answer for " + question.getCode());
                }
                for (Object item : values) {
                    if (!(item instanceof String option) || (!optionKeys.isEmpty() && !optionKeys.contains(option))) {
                        throw badRequest("Invalid option for " + question.getCode());
                    }
                    if (!selected.add(option)) throw badRequest("Duplicate option for " + question.getCode());
                }
            } else if (question.getType() == QuestionType.SINGLE) {
                if (!(value instanceof String option) || option.isBlank() || (!optionKeys.isEmpty() && !optionKeys.contains(option))) {
                    throw badRequest("Invalid answer for " + question.getCode());
                }
                selected.add(option);
            } else {
                if (!(value instanceof String text) || text.isBlank() || text.length() > 4000) {
                    if (question.isRequired() || value != null) throw badRequest("Invalid answer for " + question.getCode());
                }
            }

            String otherValue = submission.safeOtherAnswers().get(question.getCode());
            boolean allowsFreeText = selected.stream().anyMatch(option ->
                    question.getOptions().stream().anyMatch(item ->
                            item.getOptionKey().equals(option) && item.isAllowsFreeText()));
            if (otherValue != null && (otherValue.isBlank() || otherValue.length() > 1000 || !allowsFreeText)) {
                throw badRequest("Invalid additional text for " + question.getCode());
            }
            if (allowsFreeText && (otherValue == null || otherValue.isBlank())) {
                throw badRequest("Additional text is required for " + question.getCode());
            }
        }

        for (String code : submission.safeOtherAnswers().keySet()) {
            if (!answers.containsKey(code)) throw badRequest("Additional text has no answer: " + code);
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
