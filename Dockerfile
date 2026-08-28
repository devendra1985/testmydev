# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY zscaler-ca.crt /tmp/zscaler-ca.crt
RUN keytool -import -trustcacerts -alias zscaler-root \
    -file /tmp/zscaler-ca.crt \
    -keystore $JAVA_HOME/lib/security/cacerts \
    -storepass changeit -noprompt
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn ./.mvn
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
