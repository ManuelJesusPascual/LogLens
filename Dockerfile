FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# Strip CR in case the wrapper was checked out with Windows line endings
RUN sed -i 's/\r$//' mvnw && sh mvnw -B -q dependency:go-offline
COPY src src
# Tests run in CI, not while building the image
RUN sh mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd --system loglens && mkdir /data && chown loglens /data
COPY --from=build /app/target/loglens-*.jar /app.jar
USER loglens
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/data/loglens.db
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
