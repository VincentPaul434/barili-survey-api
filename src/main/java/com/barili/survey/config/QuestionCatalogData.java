package com.barili.survey.config;

import com.barili.survey.question.Question;
import com.barili.survey.question.QuestionRepository;
import com.barili.survey.question.QuestionOption;
import com.barili.survey.question.QuestionType;
import com.barili.survey.question.UserGroup;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuestionCatalogData {
    private static final Map<String, List<String>> OPTIONS = Map.ofEntries(
            Map.entry("A1", List.of("below_12", "12_14", "15_17", "18_or_older", "prefer_not")),
            Map.entry("A2", List.of("female", "male", "self_describe", "prefer_not")),
            Map.entry("A3", List.of("junior_high", "senior_high", "college", "technical", "other")),
            Map.entry("A4", List.of("very_easy", "easy", "difficult", "very_difficult")),
            Map.entry("A5", List.of("school_library", "municipal_library", "computer_area", "study_room", "coffee_or_outside", "other")),
            Map.entry("A6", List.of("home", "classroom", "school_library", "municipal_library", "coffee_shop", "internet_cafe", "online", "none", "other")),
            Map.entry("A7", List.of("individual_study", "group_study", "research", "reading", "digital_research", "printing", "workshops", "other")),
            Map.entry("A8", List.of("quiet_reading", "group_study_room", "computer_internet", "digital_learning", "history_area", "multimedia", "flexible_room", "coworking", "performance", "student_exhibit", "creative_project", "other")),
            Map.entry("A9", List.of("interactive_screens", "virtual_tours", "local_skill_demo", "traditional_crafts", "recorded_stories", "makerspace", "exhibits_events", "other")),
            Map.entry("B1", List.of("below_18", "18_24", "25_34", "35_44", "45_54", "55_64", "65_or_older", "prefer_not")),
            Map.entry("B2", List.of("female", "male", "prefer_not", "self_describe")),
            Map.entry("B3", List.of("student", "government_employee", "private_employee", "business_owner", "farmer_fisher", "product_maker", "tourism_worker", "homemaker", "retired", "other")),
            Map.entry("B4", List.of("far_from_residents", "hard_to_find", "not_enough_community_space", "no_history_place", "no_tourism_information", "no_waiting_area", "not_enough_toilets", "other")),
            Map.entry("B5", List.of("reading_information", "history_culture", "tourism_information", "community_events", "local_products", "cultural_activities", "computers_internet", "public_toilet_rest", "other")),
            Map.entry("B6", List.of("books_information_computers", "history_tourism_information", "community_events", "cultural_performances", "sell_local_products", "resident_visitor_activities", "senior_access", "toilets_seating", "other")),
            Map.entry("B7", List.of("visitor_welcome", "public_library", "interactive_museum", "local_food_products", "cooking_farming_crafts", "community_events", "cultural_shows", "outdoor_garden", "accessible_toilets", "other")),
            Map.entry("B8", List.of("solar_charging", "local_plant_garden", "promote_local_business", "livelihood_training", "flexible_room", "comfortable_inclusive_area", "tourism_maps_guides", "shaded_rest_area", "digital_museum", "other")),
            Map.entry("B9", List.of("tour_guide", "cooking_farming_crafts", "sell_local_products", "cultural_event_space", "weekend_market", "meeting_event_space", "tour_booking", "shared_group_space", "other")),
            Map.entry("C1", List.of("18_24", "25_34", "35_44", "45_59", "60_or_older", "prefer_not")),
            Map.entry("C2", List.of("female", "male", "prefer_not")),
            Map.entry("C3", List.of("barili", "another_cebu_town", "outside_cebu", "outside_philippines", "prefer_not")),
            Map.entry("C4", List.of("historical_cultural", "natural_attractions", "churches", "local_food", "markets", "festivals", "parks", "library_museum_tourism", "other")),
            Map.entry("C5", List.of("clear_signs_maps", "history_culture_information", "local_tour_guide", "food_products_information", "sit_and_rest", "public_toilets", "safe_paths", "visitor_activities", "other")),
            Map.entry("C6", List.of("difficult_to_find", "no_signs_maps", "not_enough_information", "not_enough_seats", "not_enough_toilets", "few_activities", "no_local_products", "accessibility_issue", "other")),
            Map.entry("C7", List.of("visitor_welcome", "interactive_museum", "library_reading", "digital_maps", "local_food_products", "cooking_crafts", "cultural_shows", "outdoor_garden", "accessible_toilets", "other")),
            Map.entry("C8", List.of("cultural_garden", "video_360", "story_booth", "old_new_projection", "product_workshop", "food_learning", "tour_planning", "community_exhibit", "other")),
            Map.entry("C9", List.of("tour_guide", "food_product_stalls", "craft_workshops", "cultural_shows", "weekend_markets", "tour_booking", "event_rental", "food_demonstrations", "other")),
            Map.entry("D1", List.of("planning", "engineering", "tourism_center", "municipal_library", "culture_heritage", "mayors_office", "other")),
            Map.entry("D2", List.of("less_than_year", "1_5_years", "6_10_years", "more_than_10", "prefer_not")),
            Map.entry("D3", List.of("spaces_small", "hard_to_find", "not_enough_public_space", "not_enough_staff_space", "not_enough_storage", "not_enough_toilets", "not_accessible", "services_not_connected", "other")),
            Map.entry("D5", List.of("library_reading", "museum_display", "tourism_information", "community_programs", "staff_office", "records_storage", "waiting_rest", "other")),
            Map.entry("D6", List.of("easier_to_find", "one_clear_place", "culture_learning_tourism", "better_program_rooms", "promote_history_products", "service_coordination", "other")),
            Map.entry("D7", List.of("spaces_small", "crowded_activities", "hard_to_find", "not_enough_public_programs", "not_enough_staff_space", "not_enough_storage", "better_information", "promote_history_culture", "connect_services", "other")),
            Map.entry("D8", List.of("reading_library", "history_display", "tourism_information", "waiting_rest", "meeting_training", "staff_work", "records_storage", "public_programs", "comfort_room", "signs_directions", "other")),
            Map.entry("D9", List.of("meeting_room", "event_space", "product_stalls", "craft_shop", "cafe_food", "paid_workshops", "tour_booking", "exhibit_area", "other"))
    );

    @Bean
    CommandLineRunner seedQuestions(QuestionRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                repository.findAll().stream()
                        .filter(question -> question.getOptions().isEmpty())
                        .forEach(question -> {
                            addOptions(question);
                            repository.save(question);
                        });
                return;
            }
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
        Question question = new Question(code, number, group, type, promptKey, type != QuestionType.TEXT);
        addOptions(question);
        return question;
    }

    private static void addOptions(Question question) {
        List<String> optionKeys = OPTIONS.getOrDefault(question.getCode(), List.of());
        for (int index = 0; index < optionKeys.size(); index++) {
            String optionKey = optionKeys.get(index);
            question.addOption(new QuestionOption(optionKey, index + 1,
                    optionKey.equals("other") || optionKey.equals("self_describe")));
        }
    }
}
