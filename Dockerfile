FROM gradle:9.3.1-jdk25 AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew clean bootJar -x test --no-daemon --stacktrace --info

FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
