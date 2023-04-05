FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package # this will cache the maven dependencies
#RUN ./mvnw clean package # delete above line and uncomment this if you can't get buildkit to work
CMD ["java", "-jar", "/app/target/product-starter-1.0-SNAPSHOT.jar"]
