FROM openjdk:15
COPY src /opt/src
COPY gradle /opt/gradle
COPY build.gradle /opt/
COPY gradlew /opt/
COPY settings.gradle /opt/
WORKDIR /opt/
RUN bash gradlew --no-daemon classes
ENTRYPOINT ["bash", "gradlew", "--no-daemon", "run"]