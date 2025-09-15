FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /usrapp/bin
ENV PORT=8080

COPY --from=build /app/target/classes ./classes

CMD ["bash","-lc","java -cp ./classes ServidorWeb.MicroSpringBoot"]

EXPOSE 8080
