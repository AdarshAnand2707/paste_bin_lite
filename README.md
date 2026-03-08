# PasteBin Lite

A lightweight, fast, and secure pastebin service built with Spring Boot. Share text snippets with optional expiration times and view limits.

## Features

- **User Authentication**: Register and login with secure password hashing
- **Public & Private Pastes**: Control visibility of your pastes
- **Paste Sharing**: Share private pastes with specific users
- **User Dashboard**: View all your pastes and pastes shared with you
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
- **Security**: Spring Security with BCrypt password hashing
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
- **User Registration**: http://localhost:8080/register
- **User Login**: http://localhost:8080/login
- **User Dashboard**: http://localhost:8080/dashboard (requires login)
- **Health Check**: http://localhost:8080/api/healthz
- **Actuator**: http://localhost:8080/actuator/health

## Quick Start Guide

### Create Your First Paste (Anonymous)

1. Visit http://localhost:8080
2. Enter text in the content area
3. (Optional) Set expiration time or view limit
4. Click "Create Paste"
5. Share the generated URL

### Create a Private Paste (Authenticated)

1. Register at http://localhost:8080/register
2. Log in at http://localhost:8080/login
3. Visit the home page
4. Enter your content
5. Select "PRIVATE" visibility
6. Choose users to share with from the dropdown
7. Click "Create Paste"
8. Only you and selected users can access the paste

### View Your Pastes

1. Log in to your account
2. Visit http://localhost:8080/dashboard
3. See all your created pastes under "My Pastes"
4. See pastes shared with you under "Shared with Me"

## User Authentication

### Anonymous vs Authenticated Usage

**Without Login** (Anonymous):
- Create public pastes
- View any public paste
- Cannot create private pastes
- No dashboard access

**With Login** (Authenticated):
- Create public or private pastes
- Share private pastes with specific users
- View personal dashboard with owned and shared pastes
- Full access control over paste visibility

### Register a New User

Users can register via the web interface at `/register`.

**Web**: Navigate to http://localhost:8080/register and fill in:
- Username (unique)
- Email (unique)
- Password (will be BCrypt hashed)

### Login

**Web**: Navigate to http://localhost:8080/login

Authentication is session-based. Once logged in, users can:
- Create public or private pastes
- Share private pastes with specific users
- View their dashboard with all owned and shared pastes

## API Endpoints

### Create a Paste

```http
POST /api/pastes
Content-Type: application/json

{
  "content": "Your text here",
  "ttl_seconds": 3600,
  "max_views": 10,
  "visibility": "PUBLIC",
  "sharedWith": ["username1", "username2"]
}
```

**Parameters**:
- `content` (required): The paste content
- `ttl_seconds` (optional): Time-to-live in seconds
- `max_views` (optional): Maximum number of views before expiration
- `visibility` (optional): "PUBLIC" or "PRIVATE" (default: PUBLIC, requires login for PRIVATE)
- `sharedWith` (optional): Array of usernames to share with (only for PRIVATE pastes)

**Response**:
```json
{
  "id": "abc123xyz",
  "url": "http://localhost:8080/p/abc123xyz"
}
```

**Notes**:
- Public pastes can be created by anyone (logged in or anonymous)
- Private pastes require authentication and can only be viewed by the owner and users in the `sharedWith` list

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

## User Dashboard

Authenticated users can access their dashboard at `/dashboard` to:

### My Pastes
- View all pastes they've created
- See visibility status (PUBLIC/PRIVATE)
- Check expiration times and view counts
- See how many users each private paste is shared with
- Direct links to view each paste

### Shared with Me
- View all private pastes shared with them
- See who owns/shared each paste
- Access paste metadata (creation date, expiration, views)
- Direct links to view shared pastes

**Access**: Navigate to http://localhost:8080/dashboard (requires authentication)

## Private Paste Sharing

### How It Works

1. **Create an Account**: Register and log in at `/register` and `/login`
2. **Create a Private Paste**:
   - Select "PRIVATE" visibility when creating a paste
   - Choose users to share with from the dropdown (multi-select)
3. **Access Control**:
   - Only the owner and selected users can view the paste
   - Attempting to access without permission returns 403 Forbidden
4. **Share Management**:
   - Sharing is set at paste creation time
   - Shared users are associated via the `paste_shares` join table

### Use Cases

- Share sensitive information with specific team members
- Collaborate on code snippets with selected users
- Control access to confidential notes or configurations

## Persistence Layer

### Database: MySQL

The application uses **MySQL 8.x** as the persistence layer with the following characteristics:

- **ORM**: Spring Data JPA with Hibernate
- **Schema Management**: Automatic DDL generation (`spring.jpa.hibernate.ddl-auto=update`)
- **Connection Pool**: HikariCP (default in Spring Boot)
- **Dialect**: MySQL 8 dialect for optimized SQL generation

### Entity Model

#### User Entity
The `User` entity includes:
- `id` (String, UUID): Auto-generated unique identifier
- `username` (String, unique): User's login name
- `email` (String, unique): User's email address
- `password` (String): BCrypt hashed password
- `enabled` (Boolean): Account status
- `createdAt` (LocalDateTime): Account creation timestamp

#### Paste Entity
The `Paste` entity includes:
- `id` (String, UUID): Auto-generated unique identifier
- `content` (MEDIUMTEXT): Paste content (max 16MB)
- `createdAt` (LocalDateTime): Timestamp of creation
- `expiresAt` (LocalDateTime): Optional expiration timestamp
- `maxViews` (Integer): Optional maximum view count
- `viewCount` (Integer): Current view count
- `owner` (User, FK): The user who created the paste (null for anonymous pastes)
- `visibility` (Enum): PUBLIC or PRIVATE
- `sharedWith` (Set<User>): Users with whom a PRIVATE paste is shared (many-to-many)

### Database Schema

Tables are automatically created and updated by Hibernate. The main table structures:

```sql
-- Users table
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
);

-- Pastes table
CREATE TABLE paste (
    id VARCHAR(255) PRIMARY KEY,
    content MEDIUMTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    expires_at DATETIME,
    max_views INT,
    view_count INT NOT NULL DEFAULT 0,
    owner_id VARCHAR(255),
    visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- Paste sharing join table (many-to-many)
CREATE TABLE paste_shares (
    paste_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (paste_id, user_id),
    FOREIGN KEY (paste_id) REFERENCES paste(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

Indexes are automatically created on primary keys and foreign keys for fast lookups.

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

- **Authentication**: Spring Security with session-based authentication
- **Password Security**: BCrypt hashing with strength 10
- **Access Control**:
  - Public pastes accessible to anyone
  - Private pastes restricted to owner and explicitly shared users
  - Returns 403 Forbidden for unauthorized access attempts
- **XSS Protection**: Thymeleaf's automatic HTML escaping
- **SQL Injection Prevention**: JPA parameterized queries
- **Input Validation**: Jakarta Bean Validation
- **Content Size Limits**: 16MB maximum per paste
- **CSRF Protection**: Enabled for web forms, disabled for REST API endpoints
