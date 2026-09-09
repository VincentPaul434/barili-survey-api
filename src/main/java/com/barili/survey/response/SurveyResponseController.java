package com.barili.survey.response;

import com.barili.survey.link.SurveyLinkService;
import com.barili.survey.question.Question;
import com.barili.survey.question.QuestionRepository;
import com.barili.survey.question.QuestionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

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
    public SurveySubmissionResult submit(@Valid @RequestBody SurveySubmission submission) {
        surveyLinkService.consume(submission.linkToken(), submission.userGroup());
        SurveyResponse response = new SurveyResponse(submission.userGroup(), submission.locale());
        Map<String, Object> answers = submission.answers();

        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            Question question = questionRepository.findByCode(entry.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Unknown question code: " + entry.getKey()));
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
}
