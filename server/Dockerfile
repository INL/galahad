FROM gradle:8.10-jdk17-alpine AS build
# Note: the gradle version must match the version in build.gradle.kts
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN echo "Contents of workdir:" && ls -l . \
    && gradle build --no-daemon -x test \
    && echo "Build complete, contents of build/libs:" && ls -l build/libs \
    && rm build/libs/*-plain.jar \
    && echo "Contents of build/libs after removing plain jars:" && ls -l build/libs

FROM eclipse-temurin:17-jre-alpine

# FIX CVE-2023-52425
RUN apk --no-cache add --upgrade expat

EXPOSE 8010:8080
COPY --from=build /home/gradle/src/build/libs/*.jar galahad-server.jar
COPY --from=build /home/gradle/src/application.properties application.properties

COPY /data ./data

# -Dlog4j2.formatMsgNoLookups=true is to mitigate log4j zero-day, which we are probably vulnerable to since we use
# https://logging.apache.org/log4j/kotlin/ , however it is not clear how to updrage log4j itself whilst keep using the kotlin wrapper
# Therefore we use a mitigation noted here: https://github.com/advisories/GHSA-jfh8-c2jp-5v3q
# It can be removed once we have ensured the log4j verion is properly upgraded
# We start with a big heap size to avoid OOM on large documents
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-Dspring.profiles.active=prod", "-Dlog4j2.formatMsgNoLookups=true", "-Djava.security.egd=file:/dev/./urandom","-jar","galahad-server.jar"]
