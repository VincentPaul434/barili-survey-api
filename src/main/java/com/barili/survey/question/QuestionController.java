package com.barili.survey.question;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public List<Question> list(@RequestParam(required = false) UserGroup group) {
        return group == null
                ? questionRepository.findAll()
                : questionRepository.findAllByUserGroupOrderByDisplayNumber(group);
    }
}
