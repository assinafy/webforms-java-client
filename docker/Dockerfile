FROM maven:3.9.16-eclipse-temurin-25@sha256:dde1fa3cdee15fc123221a5b325b30eb30d74c9a3433305704cdcbc1ce67b111 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn test-compile -B -q

FROM build AS test
ENTRYPOINT ["mvn", "test", "-B"]
