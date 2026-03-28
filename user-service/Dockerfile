# ─── Stage 1: Build ───
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy root (parent) POM first
COPY pom.xml ./pom.xml

# Copy module POM and source
COPY user-service/pom.xml ./user-service/pom.xml
COPY user-service/src ./user-service/src

# Install Maven and build the module
RUN apk add --no-cache maven && mvn -f user-service/pom.xml clean package -DskipTests

# ─── Stage 2: Runtime ───
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/user-service/target/user-service-1.0.0.jar ./user-service.jar

# Run the service
ENTRYPOINT ["java","-jar","/app/user-service.jar"]