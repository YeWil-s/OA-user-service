FROM eclipse-temurin:17-jre-alpine
ARG MODULE_NAME
ARG SERVICE_PORT=8080
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY ${MODULE_NAME}/target/*.jar app.jar
EXPOSE ${SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
