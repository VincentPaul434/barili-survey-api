package com.barili.survey.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "survey.admin-username=admin",
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

    @BeforeEach
    void seedTestAdmin() {
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

        String submission = "{\"linkToken\":\"" + token
                + "\",\"userGroup\":\"STUDENT\",\"locale\":\"en\","
                + "\"answers\":{},\"otherAnswers\":{}}";
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
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-admin\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("admin_session").getValue();
    }
}
