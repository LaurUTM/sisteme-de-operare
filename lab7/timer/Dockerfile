# Etapa 1: compilare cu JDK
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY TimerApp.java .
RUN javac TimerApp.java

# Etapa 2: doar JRE (imagine mai mica, fara compilator)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /src/*.class ./
CMD ["java", "TimerApp"]
