FROM gradle:jdk8
COPY src /opt/src
COPY build.gradle /opt/
COPY settings.gradle /opt/
WORKDIR /opt/
RUN gradle --no-daemon classes
ENTRYPOINT ["gradle", "--no-daemon", "run"]