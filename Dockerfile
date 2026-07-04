FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml /app/
COPY src/ /app/src/
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --gid 1000 appuser \
 && useradd --uid 1000 --gid 1000 --create-home --shell /bin/bash appuser

COPY --from=builder /app/target/grpc-virtual-thread-server-*.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar

USER appuser

EXPOSE 9090

ENTRYPOINT ["java", "-Djdk.tracePinnedThreads=full", "-jar", "/app/app.jar"]
