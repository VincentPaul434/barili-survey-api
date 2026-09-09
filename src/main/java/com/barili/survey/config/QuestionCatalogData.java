package com.barili.survey.config;

import com.barili.survey.question.Question;
import com.barili.survey.question.QuestionRepository;
import com.barili.survey.question.QuestionType;
import com.barili.survey.question.UserGroup;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuestionCatalogData {
    @Bean
    CommandLineRunner seedQuestions(QuestionRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            repository.saveAll(List.of(
                    question("A1", 1, UserGroup.STUDENT, QuestionType.SINGLE, "question.A1"),
                    question("A2", 2, UserGroup.STUDENT, QuestionType.SINGLE, "question.A2"),
                    question("A3", 3, UserGroup.STUDENT, QuestionType.SINGLE, "question.A3"),
                    question("A4", 4, UserGroup.STUDENT, QuestionType.SINGLE, "question.A4"),
                    question("A5", 5, UserGroup.STUDENT, QuestionType.MULTI, "question.A5"),
                    question("A6", 6, UserGroup.STUDENT, QuestionType.SINGLE, "question.A6"),
                    question("A7", 7, UserGroup.STUDENT, QuestionType.MULTI, "question.A7"),
                    question("A8", 8, UserGroup.STUDENT, QuestionType.MULTI, "question.A8"),
                    question("A9", 9, UserGroup.STUDENT, QuestionType.MULTI, "question.A9"),
                    question("A10", 10, UserGroup.STUDENT, QuestionType.TEXT, "question.A10"),
                    question("A11", 11, UserGroup.STUDENT, QuestionType.TEXT, "question.A11"),
                    question("A12", 12, UserGroup.STUDENT, QuestionType.TEXT, "question.A12"),
                    question("B1", 1, UserGroup.COMMUNITY_RESIDENT, QuestionType.SINGLE, "question.B1"),
                    question("B2", 2, UserGroup.COMMUNITY_RESIDENT, QuestionType.SINGLE, "question.B2"),
                    question("B3", 3, UserGroup.COMMUNITY_RESIDENT, QuestionType.SINGLE, "question.B3"),
                    question("B4", 4, UserGroup.COMMUNITY_RESIDENT, QuestionType.SINGLE, "question.B4"),
                    question("B5", 5, UserGroup.COMMUNITY_RESIDENT, QuestionType.MULTI, "question.B5"),
                    question("B6", 6, UserGroup.COMMUNITY_RESIDENT, QuestionType.MULTI, "question.B6"),
                    question("B7", 7, UserGroup.COMMUNITY_RESIDENT, QuestionType.MULTI, "question.B7"),
                    question("B8", 8, UserGroup.COMMUNITY_RESIDENT, QuestionType.MULTI, "question.B8"),
                    question("B9", 9, UserGroup.COMMUNITY_RESIDENT, QuestionType.MULTI, "question.B9"),
                    question("B10", 10, UserGroup.COMMUNITY_RESIDENT, QuestionType.TEXT, "question.B10"),
                    question("B11", 11, UserGroup.COMMUNITY_RESIDENT, QuestionType.TEXT, "question.B11"),
                    question("B12", 12, UserGroup.COMMUNITY_RESIDENT, QuestionType.TEXT, "question.B12"),
                    question("C1", 1, UserGroup.TOURIST_VISITOR, QuestionType.SINGLE, "question.C1"),
                    question("C2", 2, UserGroup.TOURIST_VISITOR, QuestionType.SINGLE, "question.C2"),
                    question("C3", 3, UserGroup.TOURIST_VISITOR, QuestionType.SINGLE, "question.C3"),
                    question("C4", 4, UserGroup.TOURIST_VISITOR, QuestionType.MULTI, "question.C4"),
                    question("C5", 5, UserGroup.TOURIST_VISITOR, QuestionType.MULTI, "question.C5"),
                    question("C6", 6, UserGroup.TOURIST_VISITOR, QuestionType.MULTI, "question.C6"),
                    question("C7", 7, UserGroup.TOURIST_VISITOR, QuestionType.MULTI, "question.C7"),
                    question("C8", 8, UserGroup.TOURIST_VISITOR, QuestionType.MULTI, "question.C8"),
                    question("C9", 9, UserGroup.TOURIST_VISITOR, QuestionType.MULTI, "question.C9"),
                    question("C10", 10, UserGroup.TOURIST_VISITOR, QuestionType.TEXT, "question.C10"),
                    question("C11", 11, UserGroup.TOURIST_VISITOR, QuestionType.TEXT, "question.C11"),
                    question("C12", 12, UserGroup.TOURIST_VISITOR, QuestionType.TEXT, "question.C12"),
                    question("D1", 1, UserGroup.LGU_PERSONNEL, QuestionType.SINGLE, "question.D1"),
                    question("D2", 2, UserGroup.LGU_PERSONNEL, QuestionType.SINGLE, "question.D2"),
                    question("D3", 3, UserGroup.LGU_PERSONNEL, QuestionType.MULTI, "question.D3"),
                    question("D5", 5, UserGroup.LGU_PERSONNEL, QuestionType.SINGLE, "question.D5"),
                    question("D6", 6, UserGroup.LGU_PERSONNEL, QuestionType.SINGLE, "question.D6"),
                    question("D7", 7, UserGroup.LGU_PERSONNEL, QuestionType.MULTI, "question.D7"),
                    question("D8", 8, UserGroup.LGU_PERSONNEL, QuestionType.MULTI, "question.D8"),
                    question("D9", 9, UserGroup.LGU_PERSONNEL, QuestionType.MULTI, "question.D9"),
                    question("D10", 10, UserGroup.LGU_PERSONNEL, QuestionType.TEXT, "question.D10"),
                    question("D11", 11, UserGroup.LGU_PERSONNEL, QuestionType.TEXT, "question.D11"),
                    question("D12", 12, UserGroup.LGU_PERSONNEL, QuestionType.TEXT, "question.D12")
            ));
        };
    }

    private static Question question(String code, int number, UserGroup group,
                                     QuestionType type, String promptKey) {
        return new Question(code, number, group, type, promptKey, type != QuestionType.TEXT);
    }
}
