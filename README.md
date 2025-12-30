# PasteBin Lite

A lightweight, fast, and secure pastebin service built with Spring Boot. Share text snippets with optional expiration times and view limits.

## Features

- Create and share text pastes instantly
- Optional time-based expiration (TTL in seconds)
- Optional view-based expiration (maximum views limit)
- Secure content rendering (XSS protection)
- RESTful API for programmatic access
- Clean web interface for browser usage
- Comprehensive health checks and monitoring

## Tech Stack

- **Backend**: Spring Boot 4.0.1, Java 17
- **Database**: MySQL 8.x
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Jakarta Bean Validation
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- MySQL 8.x running locally or remotely
- Maven 3.x (or use included Maven wrapper)

## Local Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd pastebinlite
```

### 2. Configure Database

Update the database connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pastebinlite
spring.datasource.username=your_username
spring.datasource.password=your_password
```

Or use environment variables for MySQL connection (if using a remote database like Railway).

### 3. Create Database

```sql
CREATE DATABASE pastebinlite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The application will automatically create tables on startup using Hibernate DDL auto-update.

### 4. Run the Application

Using Maven wrapper (recommended):

```bash
./mvnw spring-boot:run
```

Or using Maven directly:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Access the Application

- **Web Interface**: http://localhost:8080
- **Health Check**: http://localhost:8080/api/healthz
- **Actuator**: http://localhost:8080/actuator/health

## API Endpoints

### Create a Paste

```http
POST /api/pastes
Content-Type: application/json

{
  "content": "Your text here",
  "ttl_seconds": 3600,
  "max_views": 10
}
```

**Response**:
```json
{
  "id": "abc123xyz",
  "url": "http://localhost:8080/p/abc123xyz"
}
```

### View a Paste (API)

```http
GET /api/pastes/{id}
```

**Response**:
```json
{
  "content": "Your text here",
  "remaining_views": 9,
  "expires_at": "2025-12-31T23:59:59"
}
```

### View a Paste (Browser)

```
GET /p/{id}
```

Returns HTML page with the paste content.

## Persistence Layer

### Database: MySQL

The application uses **MySQL 8.x** as the persistence layer with the following characteristics:

- **ORM**: Spring Data JPA with Hibernate
- **Schema Management**: Automatic DDL generation (`spring.jpa.hibernate.ddl-auto=update`)
- **Connection Pool**: HikariCP (default in Spring Boot)
- **Dialect**: MySQL 8 dialect for optimized SQL generation

### Entity Model

The `Paste` entity includes:
- `id` (String, UUID): Auto-generated unique identifier
- `content` (MEDIUMTEXT): Paste content (max 16MB)
- `createdAt` (LocalDateTime): Timestamp of creation
- `expiresAt` (LocalDateTime): Optional expiration timestamp
- `maxViews` (Integer): Optional maximum view count
- `viewCount` (Integer): Current view count

### Database Schema

Tables are automatically created and updated by Hibernate. The main table structure:

```sql
CREATE TABLE paste (
    id VARCHAR(255) PRIMARY KEY,
    content MEDIUMTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME,
    max_views INT,
    view_count INT NOT NULL DEFAULT 0
);
```

Indexes are automatically created on the primary key for fast lookups.

## Configuration

### Environment Variables

- `TEST_MODE`: Set to `1` to enable test mode with custom time injection (default: `0`)

### Application Properties

Key configurations in `application.properties`:

```properties
# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# MySQL Connection
spring.datasource.url=jdbc:mysql://host:port/database
spring.datasource.username=username
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## Testing

Run tests with:

```bash
./mvnw test
```

## Building for Production

Create an executable JAR:

```bash
./mvnw clean package -DskipTests
```

Run the JAR:

```bash
java -jar target/pastebinlite-0.0.1-SNAPSHOT.jar
```

## Error Handling

The application provides consistent JSON error responses:

**404 Not Found** - Paste doesn't exist or expired:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Paste has expired"
}
```

**400 Bad Request** - Invalid input:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "content": "Content is required and must be non-empty"
  }
}
```

## Security

- XSS protection via Thymeleaf's automatic HTML escaping
- SQL injection prevention through JPA parameterized queries
- Input validation with Jakarta Bean Validation
- Content size limits (16MB maximum)
