FROM openjdk:17-alpine
ADD target/spark_java.jar spark_java.jar
ENTRYPOINT ["java", "-jar","spark_java.jar"]
EXPOSE 8080