FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=docker -Dspring.datasource.password=$DB_PASSWORD -Dsecurity.jwt.secret=$JWT_SECRET -Dapp.cors.allowed-origin=$CORS_ORIGIN -Dapp.admin.email=$ADMIN_EMAIL -Dapp.admin.senha=$ADMIN_SENHA -jar app.jar"]
