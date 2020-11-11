FROM openjdk:11
RUN apt update -y && apt install -y build-essential zlib1g-dev musl-tools && rm -rf /var/lib/apt/lists/* && ln -s /lib/libz.so.1 /lib/libz.so
COPY src /opt/src
COPY gradle /opt/gradle
COPY build.gradle /opt/
COPY gradlew /opt/
COPY settings.gradle /opt/
WORKDIR /opt/
RUN sh gradlew --no-daemon nativeImage
CMD ["/opt/build/graal/gmail-forwarder"]

FROM alpine:latest
RUN apk add --no-cache ca-certificates
COPY --from=0 /opt/build/graal/gmail-forwarder /opt/gmail-forwarder
WORKDIR /opt
CMD ["./gmail-forwarder"]