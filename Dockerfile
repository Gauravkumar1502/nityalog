FROM gradle:8.5-jdk17 AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

# Build Spring Boot jar
RUN ./gradlew clean bootJar -x test --no-daemon


# =========================
# Run stage
# =========================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
