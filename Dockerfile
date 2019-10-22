FROM openjdk:8-jdk-alpine

WORKDIR /app
ARG JAR_FILE=target/actionengine-*.jar

COPY ${JAR_FILE} /app/actionengine.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/actionengine.jar"]