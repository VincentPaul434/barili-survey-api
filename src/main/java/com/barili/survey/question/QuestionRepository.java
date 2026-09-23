package com.barili.survey.question;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByCode(String code);
    Optional<Question> findByCodeAndActiveTrue(String code);
    List<Question> findAllByActiveTrueOrderByDisplayNumber();
    List<Question> findAllByUserGroupAndActiveTrueOrderByDisplayNumber(UserGroup userGroup);
}
