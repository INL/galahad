FROM gradle:9.6.1-jdk25-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM eclipse-temurin:25.0.1_8-jre-alpine-3.23
EXPOSE 8010
EXPOSE 8011
COPY --link --from=build /home/gradle/src/build/libs/galahad.jar /home/gradle/src/application.properties ./
ENTRYPOINT ["java", "-Xms4g", "-Xmx8g", "-Dspring.profiles.active=prod", "-jar","galahad.jar"]
