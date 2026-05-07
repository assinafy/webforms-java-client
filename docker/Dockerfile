FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn test-compile -B -q

FROM build AS test
ENTRYPOINT ["mvn", "test", "-B"]
