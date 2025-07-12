FROM maven:3.8.3-openjdk-17 as base
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests=true

FROM openjdk:17.0.1-jdk-slim
COPY --from=base /app/target/distribute-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]