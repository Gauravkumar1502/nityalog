FROM gradle:9.3.1-jdk25 AS build

WORKDIR /app

# Copy only gradle wrapper + build scripts first (for caching deps)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

# Download dependencies (cached layer unless gradle files change)
RUN ./gradlew dependencies --no-daemon

# Copy source code after deps are cached
COPY src src

# Build jar
RUN ./gradlew clean bootJar -x test --no-daemon


FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
