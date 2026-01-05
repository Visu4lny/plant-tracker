# Plant Tracker API

A RESTful backend application for tracking plant watering schedules. Built with Spring Boot and secured with JWT authentication.

## Features

- **User Authentication**: Secure JWT-based registration and login system
- **Plant Management**: Create, read, update, and delete plants
- **Watering Tracking**: Track when plants were last watered
- **User Isolation**: Each user can only access their own plants
- **Validation**: Comprehensive input validation on all endpoints
- **Sorting**: Sort plant listings by various criteria
- **Error Handling**: Global exception handling with meaningful error messages

## Tech Stack

### Backend
- **Spring Boot 3.5.0** - Application framework
- **Java 17** - Programming language
- **Maven 4.0.0** - Build tool
- **Spring Data JPA** - ORM and data access
- **Spring Security 6** - Security framework

### Security
- **JWT (JJWT 0.12.6)** - Token-based authentication
- **BCrypt** - Password encryption

### Database
- **PostgreSQL 15** - Relational database
- **Docker** - Containerization (optional)

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing

### Validation
- **Jakarta Bean Validation** - Input validation

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login and receive JWT | No |

### Plants

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/plants` | Create new plant | Yes |
| GET | `/api/plants?sort=name,asc` | Get all user's plants | Yes |
| PATCH | `/api/plants/{id}/last-watered` | Update watering time | Yes |
| DELETE | `/api/plants/{id}` | Delete a plant | Yes |

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 15 or higher (or Docker)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Visu4lny/plant-tracker
   cd plant-tracker
   ```

2. **Set environment variables**
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/plant_tracker
   export DB_USER=your_db_user
   export DB_PASSWORD=your_db_password
   export JWT_SECRET=your_jwt_secret_key
   export DDL_AUTO=update  # Optional: create-drop, update, validate, none
   ```

   **Note:** If environment variables are not set, the application uses these defaults:
   - DB_URL: `jdbc:postgresql://localhost:5432/plant_tracker`
   - DB_USER: `user`
   - DB_PASSWORD: `pass`
   - DDL_AUTO: `create-drop`

3. **Run with Docker (Recommended)**
   ```bash
   docker-compose up --build
   ```

   Or run locally:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`

## Usage

### 1. Register a User

```http
POST /api/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "user",
  "password": "securePassword123"
}
```

**Response:** `201 Created`
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "User registered successfully",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Login

```http
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:** `200 OK`
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful",
  "userId": null
}
```

### 3. Create a Plant

```http
POST /api/plants HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "name": "Monstera Deliciosa"
}
```

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Monstera Deliciosa",
  "lastWateredAt": null
}
```

### 4. Get All Plants

```http
GET /api/plants HTTP/1.1
Host: localhost:8080
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Monstera Deliciosa",
    "lastWateredAt": null
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "name": "Snake Plant",
    "lastWateredAt": "2024-01-15T10:30:00Z"
  }
]
```

### 5. Update Watering Time

```http
PATCH /api/plants/{id}/last-watered HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "lastWateredAt": "2024-01-15T10:30:00Z"
}
```

### 6. Delete a Plant

```http
DELETE /api/plants/{id} HTTP/1.1
Host: localhost:8080
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response:** `204 No Content`

## Project Structure

```
src/main/java/com/example/plant_tracker/
├── controller/         # REST API endpoints
│   ├── AuthController.java
│   └── PlantController.java
├── dto/                # Data Transfer Objects
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── CreatePlantRequest.java
│   ├── UpdateLastWateredRequest.java
│   └── PlantResponse.java
├── model/              # JPA entities
│   ├── User.java
│   └── Plant.java
├── repository/         # Spring Data JPA repositories
│   ├── UserRepository.java
│   └── PlantRepository.java
├── service/            # Business logic
│   ├── AuthService.java
│   ├── UserService.java
│   ├── PlantService.java
│   └── UserDetailsServiceImpl.java
├── security/           # Security configuration
│   ├── SecurityConfig.java
│   └── jwt/
│       ├── JwtAuthFilter.java
│       └── JwtUtils.java
└── exception/          # Custom exceptions
    ├── GlobalExceptionHandler.java
    ├── EmailExistsException.java
    ├── PlantExistsException.java
    └── PlantNotFoundException.java
```

## Architecture Highlights

- **Layered Architecture**: Controller → Service → Repository pattern
- **JWT Authentication**: Stateless authentication with JWT tokens
- **DTO Pattern**: Separation of API models from domain models
- **Global Exception Handling**: Centralized error handling with `@ControllerAdvice`
- **Bean Validation**: Declarative validation using Jakarta Bean Validation
- **User Context**: Automatic user extraction from JWT token

## Testing

Run all tests:
```bash
mvn test
```

**Test Coverage:**
- Unit tests for all service classes
- Controller tests with MockMvc
- JWT authentication flow testing
- Input validation testing
- 35+ passing tests

## Database Schema

```sql
-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'ROLE_USER'
);

-- Plants Table
CREATE TABLE plants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    last_watered_at TIMESTAMPTZ,
    user_id UUID NOT NULL,
    CONSTRAINT fk_plants_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_plants_name_user UNIQUE (name, user_id)
);
```

## Future Enhancements

- [ ] Password reset functionality
- [ ] Email verification
- [ ] Refresh token mechanism
- [ ] Plant photos storage
- [ ] Watering schedule reminders
- [ ] Plant species database
- [ ] API documentation with Swagger/OpenAPI
- [ ] Integration tests

## License

This project is licensed under the MIT License.
