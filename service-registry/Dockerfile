FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# ✅ Copy entire multi-module project (IMPORTANT)
COPY . .

# ✅ Build only service-registry with parent
RUN apk add --no-cache maven && mvn -pl service-registry -am clean package -DskipTests

# ✅ Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# ✅ Copy jar from correct module
COPY --from=build /app/service-registry/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]