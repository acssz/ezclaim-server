FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

# Copy build tooling first to leverage layer caching during dependency resolution
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Pre-fetch dependencies to speed up subsequent builds
RUN ./mvnw -B -ntp dependency:go-offline

# Bring in the full project and build the Spring Boot fat jar
COPY . .
RUN ./mvnw -B -DskipTests clean package \
 && JAR_FILE="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*original*' | head -n 1)" \
 && test -n "$JAR_FILE" \
 && cp "$JAR_FILE" app.jar

FROM eclipse-temurin:24-jre AS runtime
WORKDIR /app

# Run as a non-root user for better container hardening
RUN useradd --system --create-home --home-dir /app ezclaim

COPY --from=build /app/app.jar ./app.jar

EXPOSE 8080
USER ezclaim

ENTRYPOINT ["java","-jar","/app/app.jar"]
