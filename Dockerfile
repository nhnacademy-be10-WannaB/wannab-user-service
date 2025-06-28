FROM eclipse-temurin:21
ARG JAR_FILE=./target/wannab-user-service.jar
COPY ${JAR_FILE} wannab-user-service.jar

ENTRYPOINT ["java","-jar", "/wannab-user-service.jar"]