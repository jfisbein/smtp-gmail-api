# Generate Jar file
FROM maven:3.6-jdk-11-slim as jar-builder

COPY ./pom.xml ./pom.xml
RUN mvn -T4 dependency:go-offline --batch-mode

COPY ./src ./src
RUN mvn -T4 package --batch-mode -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Djacoco.skip=true

# Small container to run Jar
FROM adoptopenjdk:11-jre-hotspot

COPY --from=jar-builder target/smtp-gmail-api.jar /smtp-gmail-api.jar
ENTRYPOINT java -jar /smtp-gmail-api.jar
