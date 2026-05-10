FROM eclipse-temurin:25-jre
WORKDIR /app
COPY build/libs/gateway-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
