# Barili Survey API

Spring Boot REST API for the four-questionnaire Barili capstone survey.

## Run locally

```bash
mvn spring-boot:run
```

The default profile uses a file-backed H2 database at `./data/barili-survey`. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `DB_DRIVER` for PostgreSQL or another JDBC database.

## Endpoints

- `GET /api/questions?group=STUDENT` returns the seeded question catalog for a group.
- `POST /api/surveys` stores a response and its typed answers.

Example payload:

```json
{
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
