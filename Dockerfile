FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
RUN useradd --system --create-home survey
COPY --from=builder /build/target/barili-survey-api-0.0.1-SNAPSHOT.jar app.jar
RUN chown survey:survey app.jar
USER survey

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
