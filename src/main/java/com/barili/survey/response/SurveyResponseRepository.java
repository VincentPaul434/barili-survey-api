package com.barili.survey.response;

import com.barili.survey.question.UserGroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, UUID> {
    Page<SurveyResponse> findAllByOrderBySubmittedAtDesc(Pageable pageable);
    Page<SurveyResponse> findByUserGroupOrderBySubmittedAtDesc(UserGroup userGroup, Pageable pageable);
    long countByUserGroup(UserGroup userGroup);

    @Query("select r.userGroup, count(r.id) from SurveyResponse r group by r.userGroup")
    List<Object[]> countGroupedByUserGroup();
}
