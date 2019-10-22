FROM openjdk:8-jdk-alpine

WORKDIR /app
ARG JAR_FILE=target/actionengine-*.jar

ARG BUILD_DATE
ARG VCS_REF
ARG VERSION

LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.name="ActionEngine" \
      org.label-schema.description="action engine to handle flow message resolve and action agent management." \
      org.label-schema.url="http://www.airta.co" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/allenyinx/actionEngine" \
      org.label-schema.vendor="airta group" \
      org.label-schema.version=$VERSION \
      org.label-schema.schema-version="1.0"

COPY ${JAR_FILE} /app/actionengine.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/actionengine.jar"]