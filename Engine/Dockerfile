FROM maven:3.6.1-jdk-8-alpine AS build
ARG BASE=/usr/src/engine
COPY pom.xml ${BASE}/
COPY src ${BASE}/src
RUN mvn -f ${BASE}/pom.xml clean package -DskipTests

FROM openjdk:8-jdk-alpine
COPY --from=build /usr/src/engine/target/engine-*.jar /app/engine.jar

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/engine.jar"]