FROM gradle:9.3.1-jdk25 AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

# Build Spring Boot jar
RUN ./gradlew clean bootJar -x test --no-daemon


# =========================
# Run stage
# =========================
FROM eclipse-temurin:25-jdk

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
