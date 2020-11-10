FROM openjdk:15-alpine
COPY src /opt/src
COPY gradle /opt/gradle
COPY build.gradle /opt/
COPY gradlew /opt/
COPY settings.gradle /opt/
WORKDIR /opt/
RUN sh gradlew --no-daemon installDist

FROM openjdk:15-alpine
COPY --from=0 /opt/build/install/gmail-forwarder /opt/gmail-forwarder
WORKDIR /opt/gmail-forwarder
CMD ["sh", "bin/gmail-forwarder"]