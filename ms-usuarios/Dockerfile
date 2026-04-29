# Etapa 1: Construcción (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiamos los archivos del proyecto
COPY pom.xml .
COPY src ./src

# Docker compila y empaqueta el proyecto internamente
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Imagen más liviana, solo con el JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# CORRECCIÓN: Apuntamos EXACTAMENTE al jar ejecutable, ignorando el -plain.jar
# Nota: "ms-usuarios" viene del <artifactId> en tu pom.xml. Si se llama distinto, cambialo acá.
COPY --from=build /app/target/ms-usuarios-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el 8081 para que coincida con el puerto de tu servicio de usuarios
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]