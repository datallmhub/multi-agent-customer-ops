FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY app.jar app.jar

EXPOSE 7860

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=7860"]
