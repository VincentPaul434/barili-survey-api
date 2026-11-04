package com.barili.survey.response;

import com.barili.survey.question.QuestionType;
import com.barili.survey.question.UserGroup;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {
    @Query("""
            select new com.barili.survey.response.AdminChoiceAnswerRow(
                q.code, q.type, r.userGroup, a.singleValue, a.multiValueJson)
            from SurveyAnswer a
            join a.question q
            join a.response r
            where q.active = true
              and q.type <> :textType
              and (:userGroup is null or r.userGroup = :userGroup)
            """)
    List<AdminChoiceAnswerRow> findChoiceAnswerRows(
            @Param("userGroup") UserGroup userGroup,
            @Param("textType") QuestionType textType);

    @Query("""
            select q.code, q.userGroup, count(a.id)
            from SurveyAnswer a
            join a.question q
            where q.active = true
              and q.type = :textType
              and (:userGroup is null or q.userGroup = :userGroup)
              and a.textValue is not null
              and trim(a.textValue) <> ''
            group by q.code, q.userGroup
            """)
    List<Object[]> countOpenEndedResponses(
            @Param("userGroup") UserGroup userGroup,
            @Param("textType") QuestionType textType);

    @Query("""
            select trim(a.textValue), count(a.id)
            from SurveyAnswer a
            join a.question q
            join a.response r
            where q.code = :questionCode
              and q.active = true
              and q.type = :textType
              and (:userGroup is null or r.userGroup = :userGroup)
              and a.textValue is not null
              and trim(a.textValue) <> ''
            group by trim(a.textValue)
            order by count(a.id) desc, trim(a.textValue) asc
            """)
    List<Object[]> findCommonOpenEndedAnswers(
            @Param("questionCode") String questionCode,
            @Param("userGroup") UserGroup userGroup,
            @Param("textType") QuestionType textType,
            Pageable pageable);

    @Query("""
            select a.textValue
            from SurveyAnswer a
            join a.question q
            join a.response r
            where q.code = :questionCode
              and q.active = true
              and q.type = :textType
              and (:userGroup is null or r.userGroup = :userGroup)
              and a.textValue is not null
              and trim(a.textValue) <> ''
            order by r.submittedAt desc
            """)
    Page<String> findOpenEndedResponses(
            @Param("questionCode") String questionCode,
            @Param("userGroup") UserGroup userGroup,
            @Param("textType") QuestionType textType,
            Pageable pageable);
}
