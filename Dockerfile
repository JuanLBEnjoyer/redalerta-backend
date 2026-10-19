FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN if [ ! -f src/main/resources/application.properties ]; then cp src/main/resources/application-example.properties src/main/resources/application.properties; fi
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
