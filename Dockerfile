FROM eclipse-temurin:17-jre

WORKDIR /app
RUN useradd --system --create-home survey
COPY target/barili-survey-api-0.0.1-SNAPSHOT.jar app.jar
RUN chown survey:survey app.jar
USER survey

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
