FROM eclipse-temurin:17-jre
WORKDIR /app
COPY xxl-job-admin/target/xxl-job-admin-*.jar /app/app.jar
EXPOSE 8080
USER 65532:65532
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=70 -jar /app/app.jar ${PARAMS}"]
