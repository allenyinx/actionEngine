FROM openjdk:8-jdk-alpine

ARG VERSION=0.0.2
ARG JAR_FILE=target/airgent-${VERSION}.jar

COPY ${JAR_FILE} airgent.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/airgent.jar"]