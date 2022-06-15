FROM maven:3.8-jdk-11 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY . .



#RUN mvn clean test
RUN mvn package -DskipTests



FROM openjdk:11
EXPOSE 8082
COPY --from=builder /app/target/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
