FROM maven:3-eclipse-temurin-26-noble@sha256:166ca19b6b5fe1e924ab2d66b64ba9854c739f16210b94bbe0074b036c5c7992 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn test-compile -B -q

FROM build AS test
CMD ["mvn", "--batch-mode", "--no-transfer-progress", "verify"]
