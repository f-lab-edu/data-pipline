FROM openjdk:17-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY build/libs/app.jar /app/app.jar

RUN chown -R appuser:appgroup /app

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]