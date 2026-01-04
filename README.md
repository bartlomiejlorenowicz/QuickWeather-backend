# 🌦️ QuickWeather API

Backend application providing weather data, forecasts, air pollution information
and user-specific search history, with a focus on clean architecture, security and testability.

⚡ Run the project in 3 minutes (Docker)

The easiest way to run the project locally is using Docker Compose.
No manual database or mail configuration is required.

1️⃣ Clone the repository
```bash

git clone https://github.com/bartlomiejlorenowicz/QuickWeather-backend
cd QuickWeather-backend
```

2️⃣ Prepare environment variables

Create a .env file based on the example:
cp .env.example .env

Fill in the required values in .env:

OPEN_WEATHER_API_KEY

ACCUWEATHER_API_KEY

JWT_SECRET

JWT_RESET_SECRET

⚠️ Important
The .env file is not committed to the repository and is required to run the application locally.

The Docker image expects a pre-built JAR file.
Before running Docker Compose, build the application:

```bash

mvn clean package -DskipTests
```

3️⃣ Start the application
```bash

docker-compose up --build
```
This will start:

🐘 PostgreSQL database
📬 MailHog is used for local email testing (no real emails are sent).
☕ Spring Boot backend application

4️⃣ Access the application

Backend API:
👉 http://localhost:8080

Swagger UI (API documentation):
👉 http://localhost:8080/swagger-ui.html

MailHog UI:
👉 http://localhost:8025

## 🐳 Docker (backend only)

Build the application
mvn clean package -DskipTests

Build Docker image
docker build -t quickweather-backend .

Run the container
docker run -p 8080:8080 quickweather-backend

The application will be available at:
👉 http://localhost:8080

## 🩺 Monitoring & Health Checks

The application exposes operational health endpoints using **Spring Boot Actuator**.

Available endpoints:
- Application health:
  👉 http://localhost:8080/actuator/health
- Application info:
  👉 http://localhost:8080/actuator/info

Only `health` and `info` endpoints are publicly exposed.
All other Actuator endpoints are secured for safety reasons.

The backend container uses the Actuator health endpoint
as a **Docker health check**, ensuring the application is fully ready
before being marked as healthy.

This setup makes the application ready for:
- Docker-based monitoring
- future Kubernetes readiness/liveness probes

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

## 🎥 Demo

Short demo video presenting the main application features:
- authentication
- weather search
- user search history
- Swagger API documentation

### Swagger API
![Swagger Demo](docs/swagger-quickweather.gif)

### Full application flow (Frontend + Backend)
![QuickWeather Demo](docs/fullstack.gif)

## 👤 Author
Bartłomiej Lorenowicz – Junior Java Developer  
📍 Poland  



