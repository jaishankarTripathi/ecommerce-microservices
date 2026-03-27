# 🛒 E-Commerce Microservices — Spring Boot

A production-ready microservices architecture built with **Java 17** and **Spring Boot 3.2**, featuring service discovery, API gateway, inter-service communication with circuit breakers, and more.

---

## 🏗️ Architecture Overview

```
                          ┌─────────────────────────────┐
                          │       Client (Browser/App)  │
                          └──────────────┬──────────────┘
                                         │ HTTP
                          ┌──────────────▼──────────────┐
                          │         API Gateway          │
                          │         Port: 8080           │
                          └──────┬──────┬──────┬─────────┘
                                 │      │      │
               ┌─────────────────┘      │      └──────────────────┐
               │                        │                          │
  ┌────────────▼──────────┐  ┌──────────▼──────────┐  ┌───────────▼──────────┐
  │     User Service      │  │   Product Service    │  │    Order Service     │
  │     Port: 8082        │  │   Port: 8081         │  │    Port: 8083        │
  │  - Register/Login     │  │  - CRUD Products     │  │  - Place Orders      │
  │  - Profile CRUD       │  │  - Search/Filter     │  │  - Track Status      │
  │  - BCrypt Auth        │  │  - Stock Management  │  │  - Feign → Product   │
  └───────────────────────┘  └──────────────────────┘  └──────────────────────┘
                                                                    │
                                                         ┌──────────▼──────────┐
                                                         │ Notification Service │
                                                         │     Port: 8084       │
                                                         │  - Email Alerts      │
                                                         └──────────────────────┘

                     ┌──────────────────────────────────────┐
                     │   Service Registry (Eureka Server)   │
                     │           Port: 8761                 │
                     │  All services register here          │
                     └──────────────────────────────────────┘
```

---

## 📦 Services

| Service              | Port  | Description                                                       |
|----------------------|-------|-------------------------------------------------------------------|
| `service-registry`   | 8761  | Eureka Server — service discovery for all microservices           |
| `api-gateway`        | 8080  | Spring Cloud Gateway — single entry point, routing, logging       |
| `user-service`       | 8082  | User registration, login (BCrypt), profile management             |
| `product-service`    | 8081  | Product CRUD, search/filter, category, stock management           |
| `order-service`      | 8083  | Order lifecycle, Feign client to product-service, circuit breaker |
| `notification-service`| 8084 | Async email notifications on order events                         |

---

## 🔑 Key Features

- ✅ **Eureka Service Discovery** — all services auto-register and discover each other
- ✅ **API Gateway** — single entry point with global logging filter and CORS
- ✅ **OpenFeign** — declarative HTTP client for order → product communication
- ✅ **Resilience4j Circuit Breaker** — fallback when product-service is down
- ✅ **Spring Security + BCrypt** — password hashing in user-service
- ✅ **Spring Data JPA + H2** — in-memory DB (swap to PostgreSQL/MySQL for prod)
- ✅ **Bean Validation** — `@Valid`, `@NotBlank`, `@Positive`, etc. on all DTOs
- ✅ **Global Exception Handling** — `@RestControllerAdvice` per service
- ✅ **Pagination & Sorting** — all list endpoints support `page`, `size`, `sortBy`
- ✅ **Async Notifications** — `@Async` email dispatch via Spring Mail
- ✅ **Actuator** — health, metrics, info endpoints on every service
- ✅ **Docker Compose** — one command to run all services

---

## 🚀 Quick Start

### Option 1 — Run with Docker Compose
```bash
cd ecommerce-microservices
docker-compose up --build
```

### Option 2 — Run Each Service Locally
Start services in this order (each in a separate terminal):

```bash
# 1. Eureka Server (must start first)
cd service-registry && mvn spring-boot:run

# 2. API Gateway
cd api-gateway && mvn spring-boot:run

# 3. User Service
cd user-service && mvn spring-boot:run

# 4. Product Service
cd product-service && mvn spring-boot:run

# 5. Order Service (depends on product-service)
cd order-service && mvn spring-boot:run

# 6. Notification Service
cd notification-service && mvn spring-boot:run
```

### Build all at once
```bash
cd ecommerce-microservices
mvn clean install -DskipTests
```

---

## 📡 API Reference

All requests go through the **API Gateway** on port `8080`.

### 👤 User Service — `/api/users`

| Method | Endpoint                    | Description              |
|--------|-----------------------------|--------------------------|
| POST   | `/api/users/register`       | Register new user        |
| POST   | `/api/users/login`          | Login with email+password|
| GET    | `/api/users/{id}`           | Get user by ID           |
| GET    | `/api/users/email/{email}`  | Get user by email        |
| GET    | `/api/users`                | Get all users            |
| PUT    | `/api/users/{id}`           | Update user profile      |
| DELETE | `/api/users/{id}`           | Deactivate user          |

**Register example:**
```json
POST /api/users/register
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "address": "123 Main St, NY"
}
```

**Login example:**
```json
POST /api/users/login
{
  "email": "john@example.com",
  "password": "password123"
}
```

---

### 📦 Product Service — `/api/products`

| Method | Endpoint                        | Description                      |
|--------|---------------------------------|----------------------------------|
| POST   | `/api/products`                 | Create product                   |
| GET    | `/api/products`                 | Get all products (paginated)     |
| GET    | `/api/products/{id}`            | Get product by ID                |
| GET    | `/api/products/search`          | Search by name/category/price    |
| GET    | `/api/products/category/{cat}`  | Get by category                  |
| GET    | `/api/products/categories`      | List all categories              |
| GET    | `/api/products/bulk?ids=1,2,3`  | Batch fetch products             |
| PUT    | `/api/products/{id}`            | Update product                   |
| PATCH  | `/api/products/{id}/stock`      | Update stock level               |
| DELETE | `/api/products/{id}`            | Soft-delete product              |

**Create product example:**
```json
POST /api/products
{
  "name": "MacBook Pro 16",
  "description": "Apple M3 Pro chip",
  "price": 2499.99,
  "stockQuantity": 25,
  "category": "Computers",
  "imageUrl": "https://example.com/macbook.jpg"
}
```

**Search example:**
```
GET /api/products/search?name=mac&category=Computers&minPrice=1000&maxPrice=3000&page=0&size=10
```

---

### 🛒 Order Service — `/api/orders`

| Method | Endpoint                          | Description                      |
|--------|-----------------------------------|----------------------------------|
| POST   | `/api/orders`                     | Place a new order                |
| GET    | `/api/orders`                     | Get all orders (paginated)       |
| GET    | `/api/orders/{id}`                | Get order by ID                  |
| GET    | `/api/orders/number/{orderNum}`   | Get order by order number        |
| GET    | `/api/orders/user/{userId}`       | Get all orders for a user        |
| GET    | `/api/orders/status/{status}`     | Filter by status                 |
| PATCH  | `/api/orders/{id}/status`         | Update order status              |
| PATCH  | `/api/orders/{id}/cancel`         | Cancel an order                  |

**Place order example:**
```json
POST /api/orders
{
  "userId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ],
  "shippingAddress": "123 Main St, New York, NY 10001",
  "paymentMethod": "CREDIT_CARD",
  "notes": "Leave at door"
}
```

**Order status values:** `PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → REFUNDED`

---

### 🔔 Notification Service — `/api/notifications`

| Method | Endpoint                           | Description                 |
|--------|------------------------------------|-----------------------------|
| POST   | `/api/notifications/send`          | Send custom notification    |
| POST   | `/api/notifications/order-placed`  | Order placed email          |
| POST   | `/api/notifications/order-status`  | Order status change email   |
| POST   | `/api/notifications/welcome`       | Welcome email               |

---

## 🌐 Monitoring & Tools

| URL                                      | Description                    |
|------------------------------------------|--------------------------------|
| http://localhost:8761                    | Eureka Dashboard               |
| http://localhost:8080/actuator/health    | Gateway Health                 |
| http://localhost:8081/h2-console         | Product DB Console             |
| http://localhost:8082/h2-console         | User DB Console                |
| http://localhost:8083/h2-console         | Order DB Console               |
| http://localhost:8083/actuator/health    | Circuit Breaker Health         |

---

## 🗂️ Project Structure

```
ecommerce-microservices/
├── pom.xml                          ← Parent POM (all modules)
├── docker-compose.yml
│
├── service-registry/                ← Eureka Server
│   └── src/main/java/.../ServiceRegistryApplication.java
│
├── api-gateway/                     ← Spring Cloud Gateway
│   └── src/main/java/.../filter/LoggingFilter.java
│
├── user-service/                    ← User management
│   └── src/main/java/com/ecommerce/user/
│       ├── model/User.java
│       ├── dto/UserDto.java
│       ├── repository/UserRepository.java
│       ├── service/UserService.java
│       ├── controller/UserController.java
│       ├── exception/GlobalExceptionHandler.java
│       └── config/SecurityConfig.java
│
├── product-service/                 ← Product catalog
│   └── src/main/java/com/ecommerce/product/
│       ├── model/Product.java
│       ├── dto/ProductDto.java
│       ├── repository/ProductRepository.java
│       ├── service/ProductService.java
│       ├── controller/ProductController.java
│       └── exception/GlobalExceptionHandler.java
│
├── order-service/                   ← Order lifecycle + Feign
│   └── src/main/java/com/ecommerce/order/
│       ├── model/{Order, OrderItem}.java
│       ├── dto/OrderDto.java
│       ├── repository/OrderRepository.java
│       ├── service/OrderService.java       ← Circuit Breaker here
│       ├── controller/OrderController.java
│       ├── client/ProductClient.java       ← Feign client
│       └── client/ProductClientFallback.java
│
└── notification-service/            ← Async email
    └── src/main/java/com/ecommerce/notification/
        ├── model/NotificationRequest.java
        └── service/NotificationService.java
```

---

## ⚙️ Configuration

### Switch to PostgreSQL (Production)
Replace the H2 config in any service's `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_products
    username: postgres
    password: secret
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate   # use Flyway/Liquibase for migrations
```

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Configure Email (Notification Service)
Set environment variables:
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password   # Gmail App Password
```

---

## 🔄 Circuit Breaker Flow

When `order-service` calls `product-service`:

```
Order Service ──Feign──► Product Service
     │                        │
     │    (if unavailable)    │
     └──► Circuit Breaker ◄───┘
              │
              ▼
         Fallback Method
    (throws meaningful error)
```

States: **CLOSED** → **OPEN** (after 50% failures in 10 calls) → **HALF-OPEN** (after 5s) → **CLOSED**

---

## 🛠️ Tech Stack

| Technology              | Version  | Purpose                        |
|-------------------------|----------|--------------------------------|
| Java                    | 17       | Language                       |
| Spring Boot             | 3.2.0    | Framework                      |
| Spring Cloud            | 2023.0.0 | Microservice tooling           |
| Spring Cloud Netflix Eureka | —    | Service discovery              |
| Spring Cloud Gateway    | —        | API Gateway                    |
| Spring Cloud OpenFeign  | —        | Declarative REST client        |
| Resilience4j            | 2.1.0    | Circuit breaker                |
| Spring Security         | —        | Authentication                 |
| Spring Data JPA         | —        | ORM / Repository               |
| H2 Database             | —        | In-memory DB (dev)             |
| Lombok                  | —        | Boilerplate reduction          |
| Spring Mail             | —        | Email notifications            |
| Docker Compose          | —        | Container orchestration        |

---

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (optional, for containerized run)

---

*Built with ❤️ using Spring Boot Microservices*
