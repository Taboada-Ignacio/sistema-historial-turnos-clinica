# Etapa 1: Construcción (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiamos los archivos del proyecto API Gateway
COPY pom.xml .
COPY src ./src

# Compilamos y empaquetamos el Gateway
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Solo JRE de Java 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos EXACTAMENTE el jar del gateway (ignorando cualquier -plain.jar)
COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto 8080 (el que configuramos en el application.yml)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]