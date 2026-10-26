# Build stage: the Maven wrapper downloads Maven, the JDK compiles the app
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Dependencies first, so this layer stays cached until pom.xml changes
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B -q dependency:go-offline

# Tests need Docker (Testcontainers), so they are skipped here
COPY src src
RUN ./mvnw -B -q package -DskipTests

# Runtime stage: JRE only, running as an unprivileged user
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/room-booking-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
