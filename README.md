# Barili Survey API

Spring Boot REST API for the four-questionnaire Barili capstone survey.

## Run locally

```bash
mvn spring-boot:run
```

The default profile uses a file-backed H2 database at `./data/barili-survey`. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `DB_DRIVER` for PostgreSQL or another JDBC database.

Set `SURVEY_ADMIN_USERNAME` and `SURVEY_ADMIN_PASSWORD_HASH` to provision the initial admin account; the application has no built-in default credentials. Set `SPRING_PROFILES_ACTIVE=prod` for deployments, use PostgreSQL with `JPA_DDL_AUTO=validate`, set `SURVEY_ADMIN_COOKIE_SECURE=true`, and set `SURVEY_PUBLIC_BASE_URL` to the frontend origin used in generated respondent links. The admin logs in at `/admin/login`; the dashboard can generate single-use, expiring links and view submitted responses. Respondents open `/survey/{token}` and submit the token with their answers.

The service exposes `GET /api/health`. On Render, `RENDER_EXTERNAL_URL` is used automatically for a lightweight self-ping every 10 minutes while the process is running. Configure `SELF_PING_URL`, `SELF_PING_INTERVAL_MS`, or `SELF_PING_INITIAL_DELAY_MS` only when you need different values. Render free instances can still suspend after inactivity; use an external monitor or a paid instance when continuous availability is required.

## Endpoints

- `GET /api/questions?group=STUDENT` returns the seeded question catalog for a group.
- `POST /api/surveys` stores a response and its typed answers.
- `POST /api/admin/login` starts the HttpOnly admin session.
- `POST /api/admin/survey-links` generates a protected, single-use respondent link.
- `GET /api/admin/responses` returns all submitted responses for the admin dashboard.

Example payload:

```json
{
  "linkToken": "opaque-token-from-the-respondent-link",
  "userGroup": "STUDENT",
  "locale": "ceb",
  "answers": {
    "A1": "15_17",
    "A5": ["school_library", "study_room"],
    "A10": "A makerspace for school projects"
  },
  "otherAnswers": {}
}
```

Questionnaire D follows the PDF numbering and therefore intentionally has no `D4`.
