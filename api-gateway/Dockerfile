FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
# Asegurate de que el nombre del .jar coincida con el que genera tu pom.xml
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]