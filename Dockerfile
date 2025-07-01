FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/plant-tracker-*.jar app.jar

EXPOSE 8080

USER 1000:1000

ENTRYPOINT ["java", "-jar", "app.jar"]