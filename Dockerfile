FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY src ./src
RUN sed -i 's/\r$//' gradlew \
    && sh ./gradlew --no-daemon test installDist

FROM eclipse-temurin:21-jre-jammy
RUN groupadd --gid 10001 onegym \
    && useradd --uid 10001 --gid 10001 --no-create-home onegym \
    && install -d -o 10001 -g 10001 /app/uploads
WORKDIR /app
COPY --from=build /workspace/build/install/OneGymApi/ /app/
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=20s --start-period=30s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/health/ready"]
ENTRYPOINT ["java", "-Dconfig.resource=application-coolify.conf", "-cp", "/app/lib/*", "com.concatstudio.onegym.MainKt"]
