FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f pom.xml clean package # this will cache the maven dependencies
CMD ["java", "-jar", "/app/target/product-starter-1.0-SNAPSHOT.jar"]
