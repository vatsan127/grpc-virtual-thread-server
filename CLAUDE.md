# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project goal

Combine **Java 21 virtual threads (Loom)** with **gRPC** to improve throughput and latency for realtime systems. The intent is that blocking I/O in gRPC handlers (DB, Kafka, downstream RPCs) runs on virtual threads so concurrency scales without hand-tuned thread pools or a reactive stack. When adding server-side code, prefer straightforward blocking APIs on virtual threads over `@Async`, custom executors, or reactive workarounds — and make sure gRPC's server executor is actually a virtual-thread executor (e.g., `Executors.newVirtualThreadPerTaskExecutor()`), not just Spring MVC's.

## Status

The repository is currently a Spring Boot scaffold — only `GrpcVirtualThreadServerApplication` and an empty `application.yaml` exist. Almost all architectural intent lives in `pom.xml`; treat that as the source of truth until the code fills in.

## Common commands

Uses the Maven Wrapper — invoke `./mvnw` on Unix or `mvnw.cmd` on Windows (don't rely on a system `mvn`).

- Build (also runs Avro codegen): `./mvnw clean package`
- Run the app: `./mvnw spring-boot:run`
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=ClassName`
- Run a single test method: `./mvnw test -Dtest=ClassName#methodName`
- Regenerate Avro sources without a full build: `./mvnw generate-sources`

## Architecture — non-obvious pieces

**Virtual threads wiring is the whole point (see Project goal above).** Boot 3.5 auto-configures virtual threads for MVC/Tomcat when `spring.threads.virtual.enabled=true` — that flag alone does **not** make the gRPC server use them. The `net.devh` gRPC starter has its own executor; make sure it's set to a virtual-thread executor when handlers are added, otherwise the module's core value proposition isn't realized.

**gRPC server via `net.devh:grpc-server-spring-boot-starter` (v3.1.0.RELEASE).** The starter's bundled `grpc-netty-shaded` is explicitly *excluded* and re-added at `grpc.version` (1.70.0) so the transport version stays in lockstep with `grpc-services`, `grpc-protobuf`, and `grpc-stub`. If you bump one gRPC coord, bump the `grpc.version` property — do not add per-dep version overrides. `spring-grpc-dependencies` (0.5.0) is imported alongside for the Spring gRPC BOM.

**Avro schema codegen at build time.** The `avro-maven-plugin` reads `.avsc` files from `src/main/resources/avro/` during `generate-sources` and writes Java into `src/main/java/` (with `stringType=String`). Consequences:
- The `avro/` directory does not exist yet — create it before adding schemas.
- Generated sources land inside the tracked source tree, not `target/generated-sources`. Add generated packages to `.gitignore` when you introduce schemas, or the working copy will fill with generated files.

**Confluent stack for Kafka + Schema Registry.** `kafka-streams-avro-serde` and `kafka-avro-serializer` are pulled from the Confluent Maven repo (declared under `<repositories>`) — builds will fail behind a firewall that blocks `packages.confluent.io`. `kafka-avro-serializer` is `provided` scope, so it must be supplied by the runtime container if you actually use it.

**Databases: MySQL runtime, PostgreSQL provided.** JPA is on the classpath; the MySQL driver is `runtime` scope, PostgreSQL is `provided`. Pick one via the runtime environment — don't assume both are available at test time.

**Spring Cloud Consul is on the classpath.** `spring-cloud-starter-consul-all` + `spring-cloud-starter-bootstrap` means the app expects a Consul agent on startup unless discovery/config are explicitly disabled (`spring.cloud.consul.enabled=false`, `spring.cloud.discovery.enabled=false`). Local runs without Consul will fail with connection errors — disable it in dev profiles.

**Logging is structured.** `logstash-logback-encoder` is present so JSON-formatted logs are the intended output. Boot's default `log4j-to-slf4j` bridge is excluded from `spring-boot-starter-web` — don't reintroduce it.

**JUnit 4 and JUnit 5 both present.** `spring-boot-starter-test` brings JUnit 5 (Jupiter); `junit:junit:4.13.1` is also declared. New tests should use Jupiter unless a specific reason forces JUnit 4.
