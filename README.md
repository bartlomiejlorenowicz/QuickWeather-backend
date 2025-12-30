# 🌦️ QuickWeather API

Backend application providing weather data, forecasts, air pollution information
and user-specific search history, with focus on clean architecture, security and testability.

## 🚀 Tech Stack
- Java 21
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL
- Liquibase
- Docker / Docker Compose
- JUnit 5, Mockito
- OpenWeather API

## 🧠 Key Features
- User registration & authentication (JWT)
- Password change & reset via email token
- Weather data caching (database-level)
- User search history persistence
- External API integration (OpenWeather)
- Validation chains & centralized error handling
- Unit tests for service layer (Mockito)

## 🔐 Security
- JWT-based authentication
- Password hashing (BCrypt)
- Token validation & expiration
- Account locking support after failed login attempts

## 🧩 Architecture
- Controller → Service → Repository
- Separate validation chains
- DTO mapping layer
- Centralized exception handling

The application follows a layered architecture with thin controllers and
business logic encapsulated in the service layer.

## 🧪 Testing
- Unit tests for service layer (Mockito)
- Validation logic tested
- Edge cases covered (invalid tokens, missing data)
- External dependencies mocked (RestTemplate, repositories)

## 🖥️ Frontend

The project includes a dedicated frontend application that communicates with the QuickWeather API backend.

Frontend details:
- built with **Angular**
- consumes the backend via **REST API**
- features:
    - user registration and authentication (JWT)
    - weather search by city and coordinates
    - user-specific search history
    - integration with backend Swagger documentation

📦 Frontend repository: https://github.com/bartlomiejlorenowicz/QuickWeather-Frontend

The backend and frontend are developed as separate projects, enabling independent development and deployment.

### ▶️ Swagger UI
```text
http://localhost:8080/swagger-ui/index.html
```

## 🐳 Running locally

The application can be run locally using Docker Compose:

```bash
docker-compose up -d
```

## 🎥 Demo

Short demo video presenting the main application features:
- authentication
- weather search
- user search history

## 👤 Author
Bartłomiej Lorenowicz – Junior Java Developer


