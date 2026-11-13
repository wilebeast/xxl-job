ARG XXL_JOB_BUILD_IMAGE
ARG RISK_PREVIEW_IMAGE
FROM ${RISK_PREVIEW_IMAGE} AS risk-preview

FROM ${XXL_JOB_BUILD_IMAGE} AS build
COPY xxl-job-executor-samples/xxl-job-executor-risk-shadow/pom.xml /workspace/xxl-job-executor-samples/xxl-job-executor-risk-shadow/pom.xml
COPY xxl-job-executor-samples/xxl-job-executor-risk-shadow/src /workspace/xxl-job-executor-samples/xxl-job-executor-risk-shadow/src
RUN mvn -pl xxl-job-executor-samples/xxl-job-executor-risk-shadow -am -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre AS admin
WORKDIR /app
COPY --from=build /workspace/xxl-job-admin/target/xxl-job-admin-3.5.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
USER 65532:65532
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=70 -jar /app/app.jar ${PARAMS}"]

FROM eclipse-temurin:17-jre AS risk-shadow-executor
WORKDIR /app
COPY --from=build /workspace/xxl-job-executor-samples/xxl-job-executor-risk-shadow/target/xxl-job-executor-risk-shadow-3.5.1-SNAPSHOT.jar /app/app.jar
COPY --from=risk-preview /offline-risk /offline-risk
EXPOSE 9999
USER 65532:65532
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/app.jar"]
