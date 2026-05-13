FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /build
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY --from=builder /build/target/ghostwriter-0.0.1-SNAPSHOT.jar app.jar
COPY certs/ certs/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
