FROM eclipse-temurin:17-jre
WORKDIR /app
COPY xxl-job-executor-samples/xxl-job-executor-risk-shadow/target/xxl-job-executor-risk-shadow-*.jar /app/app.jar
EXPOSE 9999
USER 65532:65532
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/app.jar"]
