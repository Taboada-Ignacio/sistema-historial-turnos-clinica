# Etapa 1: Construcción (Maven)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Ejecutamos el empaquetado
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# CORRECCIÓN AQUÍ:
# 1. Usamos --from=build (con el signo igual)
# 2. En lugar de *.jar, buscamos uno que NO termine en -plain.jar 
#    O simplemente usamos un truco de shell para asegurar que sea uno solo.
COPY --from=build /app/target/*.jar ./app.jar

# Si el error de "multiple files" persiste, es porque Maven generó dos jars. 
# Para solucionarlo definitivamente, reemplaza la línea anterior por esta:
# COPY --from=build /app/target/ms-usuarios-0.0.1-SNAPSHOT.jar ./app.jar
# (Asegúrate de que el nombre coincida con el de tu pom.xml)

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]