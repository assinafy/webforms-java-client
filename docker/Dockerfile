FROM maven:3.9.16-eclipse-temurin-25-noble@sha256:d67198007bb4441b07d45587320f83154de80ece3608f80408ef14c6ea847753 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn test-compile -B -q

FROM build AS test
CMD ["mvn", "--batch-mode", "--no-transfer-progress", "verify"]
