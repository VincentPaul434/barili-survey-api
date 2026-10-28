package com.barili.survey.admin;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.barili.survey.link.SurveyLinkRepository;
import com.barili.survey.response.SurveyResponseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:barili-admin-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminAuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminAccountRepository adminAccountRepository;

    @Autowired
    private SurveyLinkRepository surveyLinkRepository;

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedTestAdmin() {
        surveyResponseRepository.deleteAll();
        surveyLinkRepository.deleteAll();
        if (adminAccountRepository.findByUsernameIgnoreCase("test-admin").isEmpty()) {
            adminAccountRepository.save(new AdminAccount(
                    "test-admin",
                    new BCryptPasswordEncoder(4).encode("test-password")));
        }
    }

    @Test
    void adminEndpointsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/admin/responses"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/survey-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userGroup\":\"STUDENT\",\"expiresInHours\":24}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedAdminCanGenerateAndUseOneLink() throws Exception {
        String sessionCookie = login();
        MvcResult linkResult = mockMvc.perform(post("/api/admin/survey-links")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userGroup\":\"STUDENT\",\"expiresInHours\":24}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode linkResponse = objectMapper.readTree(linkResult.getResponse().getContentAsString());
        String link = linkResponse.get("link").asText();
        String token = link.substring(link.lastIndexOf("/survey/") + "/survey/".length());

        mockMvc.perform(get("/api/survey-links/{token}", token))
                .andExpect(status().isOk());

        String submission = studentSubmission(token);
        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission))
                .andExpect(status().isGone());

        mockMvc.perform(get("/api/admin/responses")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/responses")
                        .param("userGroup", "STUDENT")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/admin/responses")
                        .param("userGroup", "LGU_PERSONNEL")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void authenticatedAdminCanDeleteResponseAndUnauthenticatedRequestsAreRejected() throws Exception {
        String sessionCookie = login();
        MvcResult linkResult = mockMvc.perform(post("/api/admin/survey-links")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userGroup\":\"STUDENT\",\"expiresInHours\":24}"))
                .andExpect(status().isCreated())
                .andReturn();
        String link = objectMapper.readTree(linkResult.getResponse().getContentAsString()).get("link").asText();
        String token = link.substring(link.lastIndexOf("/survey/") + "/survey/".length());
        String submission = studentSubmission(token);
        MvcResult submissionResult = mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission))
                .andExpect(status().isCreated())
                .andReturn();
        UUID responseId = UUID.fromString(objectMapper.readTree(submissionResult.getResponse().getContentAsString())
                .get("id").asText());

        mockMvc.perform(options("/api/admin/responses/{id}", responseId)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")));
        mockMvc.perform(delete("/api/admin/responses/{id}", responseId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/admin/responses/{id}", responseId)
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertTrue(surveyResponseRepository.findById(responseId).isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(0L, jdbcTemplate.queryForObject(
                "select count(*) from survey_answers where response_id = ?", Long.class, responseId));
        mockMvc.perform(get("/api/admin/responses")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void invalidSubmissionDoesNotConsumeLink() throws Exception {
        String sessionCookie = login();
        MvcResult linkResult = mockMvc.perform(post("/api/admin/survey-links")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userGroup\":\"STUDENT\",\"expiresInHours\":24}"))
                .andExpect(status().isCreated())
                .andReturn();
        String link = objectMapper.readTree(linkResult.getResponse().getContentAsString()).get("link").asText();
        String token = link.substring(link.lastIndexOf("/survey/") + "/survey/".length());

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"linkToken\":\"" + token + "\",\"userGroup\":\"STUDENT\","
                                + "\"locale\":\"en\",\"answers\":{\"S1\":\"s_18_20\"},\"otherAnswers\":{}}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/survey-links/{token}", token))
                .andExpect(status().isOk());
    }

    @Test
    void submissionWithoutSurveyLinkIsRejected() throws Exception {
        String submission = studentSubmission("unused").replace("\"linkToken\":\"unused\",", "");

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicTokenIsReusableAndRemainsBoundToItsQuestionnaire() throws Exception {
        String sessionCookie = login();
        MvcResult linkResult = mockMvc.perform(post("/api/admin/survey-links")
                        .cookie(new jakarta.servlet.http.Cookie("admin_session", sessionCookie))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userGroup\":\"STUDENT\",\"expiresInHours\":24,\"reusable\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reusable").value(true))
                .andReturn();
        String link = objectMapper.readTree(linkResult.getResponse().getContentAsString()).get("link").asText();
        String token = link.substring(link.lastIndexOf("/survey/") + "/survey/".length());
        String submission = studentSubmission(token);

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission.replace("\"userGroup\":\"STUDENT\"", "\"userGroup\":\"LGU_PERSONNEL\"")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submission))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/survey-links/{token}", token))
                .andExpect(status().isOk());
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-admin\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("admin_session").getValue();
    }

    private String studentSubmission(String token) {
        return "{\"linkToken\":\"" + token + "\",\"userGroup\":\"STUDENT\",\"locale\":\"en\","
                + "\"consentGiven\":true,\"answers\":{"
                + "\"S1\":\"s_18_20\",\"S2\":\"female\",\"S3\":\"college\",\"S4\":\"s_live_study_barili\","
                + "\"S5\":\"s_daily\",\"S6\":\"s_once\",\"S7\":[\"s_home\"],\"S8\":[\"s_no_difficulty\"],"
                + "\"S9\":[\"s_quiet_study\"],\"S10\":[\"individual_study\"],\"S11\":[\"s_interactive_maps\"],"
                + "\"S12\":\"s_important\",\"S13\":[\"s_more_learning\"],\"S14\":\"s_support\","
                + "\"S15\":\"Local history\",\"S16\":\"More quiet study areas\",\"S17\":[\"s_natural_light\"]},"
                + "\"otherAnswers\":{}}";
    }
}
