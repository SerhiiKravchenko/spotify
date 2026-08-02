FROM maven:4.0.0-rc-5-sapmachine-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8090

CMD ["java", "-jar", "app.jar"]