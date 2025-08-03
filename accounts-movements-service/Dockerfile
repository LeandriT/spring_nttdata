FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.h2.console.settings.web-allow-others=true", "-Dspring.h2.console.enabled=true", "-jar", "/app/app.jar"]