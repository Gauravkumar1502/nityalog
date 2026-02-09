FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy only gradle config first (better caching)
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

RUN chmod +x gradlew

# Download dependencies first (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build jar
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
