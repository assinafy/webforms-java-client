FROM maven:3-eclipse-temurin-24-noble@sha256:a137a467ec89b5713d0be817b55bdba6b4d6ef16e3d05565a79bc08d8e775a1c AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn test-compile -B -q

FROM build AS test
CMD ["mvn", "--batch-mode", "--no-transfer-progress", "verify"]
