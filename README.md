# Barili Survey API

Spring Boot REST API for the four-questionnaire Barili capstone survey.

## Run locally

```bash
mvn spring-boot:run
```

The default profile uses a file-backed H2 database at `./data/barili-survey`. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `DB_DRIVER` for PostgreSQL or another JDBC database.

The application provisions the initial `admin` account in the database using a BCrypt password hash. Set `SURVEY_ADMIN_COOKIE_SECURE=true` in HTTPS deployments and set `SURVEY_PUBLIC_BASE_URL` to the frontend origin used in generated respondent links. The admin logs in at `/admin/login`; the dashboard can generate single-use, expiring links and view submitted responses. Respondents open `/survey/{token}` and submit the token with their answers.

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
