FROM eclipse-temurin:25-jre

WORKDIR /app

COPY cdn/target/main-runnable.jar /app/main.jar

EXPOSE 1099

ENTRYPOINT ["java", "-jar", "main.jar"]
