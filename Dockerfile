FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn -Dmaven.test.skip=true package

FROM tomcat:9.0-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/omegamart.war /usr/local/tomcat/webapps/ROOT.war

RUN mkdir -p /usr/local/tomcat/data

EXPOSE 10000

CMD ["sh", "-c", "sed -i 's/port=\"8080\"/port=\"'\"${PORT:-10000}\"'\"/' /usr/local/tomcat/conf/server.xml && catalina.sh run"]