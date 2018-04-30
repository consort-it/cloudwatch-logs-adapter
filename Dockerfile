FROM openjdk:8-jdk as builder

WORKDIR /microservice


COPY src ./src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
ADD gradle gradle
COPY swagger.yaml .

RUN ls -l ./ && chmod 777 ./gradlew && ./gradlew build

FROM openjdk:8-jre

WORKDIR /microservice/

RUN curl -s -H 'X-JFrog-Art-Api:AKCp2VpEdD1yvM7ATezQkjUnwvVB9yDp6jFy2D3moAGLuhrq7eY7BMUa634exLeX1kHExi6rv' "https://consortit.jfrog.io/consortit/generic-artifacts-local/vaultenv/vaultenv" -o /usr/local/bin/vaultenv

EXPOSE 8080
EXPOSE 8081

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["java -Xmx128m -server -jar /microservice/cloudwatch-logs-adapter.jar -Duser.timezone=UTC"]

COPY --from=builder "/microservice/build/libs/cloudwatch-logs-adapter-all-*.jar" /microservice/cloudwatch-logs-adapter.jar
