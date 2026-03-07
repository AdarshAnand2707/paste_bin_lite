# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PasteBin Lite - A Spring Boot pastebin service with MySQL persistence and user authentication. Allows creating and sharing text snippets with optional time-based (TTL) and view-based expiration. Supports user registration, login, and private pastes shared with specific users.

**Tech Stack**: Spring Boot 4.0.1, Java 17, MySQL 8.x, Spring Data JPA/Hibernate, Spring Security, Thymeleaf, Maven

## Build and Run Commands

### Run the Application
```bash
./mvnw spring-boot:run
```

### Run Tests
```bash
./mvnw test
```

### Build JAR
```bash
./mvnw clean package -DskipTests
```

### Run Single Test
```bash
./mvnw test -Dtest=ClassName#methodName
```

## Database Configuration

The application requires MySQL 8.x. Database credentials are configured in `src/main/resources/application.properties`:
- Connection URL: `spring.datasource.url`
- Username: `spring.datasource.username`
- Password: `spring.datasource.password` (supports environment variable `${DB_PASSWORD}`)

Schema is auto-generated/updated via Hibernate DDL (`spring.jpa.hibernate.ddl-auto=update`).

## Architecture

### Package Structure
- `entity/` - JPA entities (Paste, User, Visibility enum)
- `repository/` - Spring Data JPA repositories
  - `PasteRepository` - Query methods for pastes (by owner, by shared users)
  - `UserRepository` - User lookup and validation
- `service/` - Business logic layer (PasteService, UserService, CustomUserDetailsService)
- `controller/` - REST API and web controllers (PasteApiController, PasteViewController, HomeController, AuthController, DashboardController, HealthController)
- `dto/` - Request/Response DTOs (CreatePasteRequest, PasteResponse, RegisterRequest, etc.)
- `exception/` - Custom exceptions and global error handler
- `config/` - Security configuration (Spring Security setup)

### Key Components

**User Entity** (`entity/User.java`):
- User authentication and authorization
- Fields: id (UUID), username (unique), email (unique), password (BCrypt hashed), enabled, createdAt
- Many-to-many relationship with Paste for sharing

**Paste Entity** (`entity/Paste.java`):
- Uses UUID generation for IDs
- Content stored as MEDIUMTEXT (max 16MB)
- Expiration logic: time-based (`expiresAt`) OR view-based (`maxViews`)
- View count automatically incremented on retrieval
- `isExpired(LocalDateTime)` method centralizes expiration checks
- **Ownership**: Links to User entity via `owner` field
- **Visibility**: Enum (PUBLIC/PRIVATE) determines access control
- **Sharing**: Many-to-many relationship with User via `sharedWith` (join table: paste_shares)

**UserService** (`service/UserService.java`):
- `registerUser()` - Creates new user with BCrypt password hashing
- `findByUsername()` - Retrieves user by username
- `findUsersByUsernames()` - Batch user lookup for sharing
- Validates username/email uniqueness

**CustomUserDetailsService** (`service/CustomUserDetailsService.java`):
- Implements Spring Security's UserDetailsService
- Loads user credentials for authentication

**PasteService** (`service/PasteService.java`):
- `createPaste()` - Creates new paste with optional TTL, max views, visibility, and sharing
  - Sets owner from authenticated user context
  - Handles visibility (PUBLIC/PRIVATE)
  - Associates shared users for PRIVATE pastes
- `getPaste()` - Retrieves paste, increments view count, enforces expiration and access control
  - **Access Control**: For PRIVATE pastes, checks if user is owner or in sharedWith list
  - Throws `AccessDeniedException` if unauthorized
- `getUserOwnedPastes()` - Returns all pastes created by a user (ordered by creation date)
- `getUserSharedPastes()` - Returns all pastes shared with a user (ordered by creation date)
- **Test Mode Support**: Enables time injection via `TEST_MODE=1` environment variable and `x-test-now-ms` header for testing time-based expiration

**Controllers**:
- `AuthController` - Login (`/login`) and registration (`/register`) pages
- `PasteApiController` - RESTful API at `/api/pastes`
- `PasteViewController` - Web UI at `/p/{id}`
- `HomeController` - Landing page at `/` (passes authenticated user and user list to template)
- `DashboardController` - User dashboard at `/dashboard` (shows owned and shared pastes)
- `HealthController` - Health check at `/api/healthz`

**Security Configuration** (`config/SecurityConfig.java`):
- BCrypt password encoding
- Form-based login with custom login page
- Public access to home, login, register, health endpoints, and paste viewing
- CSRF disabled for API endpoints
- Session-based authentication

**Exception Handling**:
- `GlobalExceptionHandler` provides consistent JSON error responses
- `PasteNotFoundException` triggers 404 responses (expired or missing pastes)
- `AccessDeniedException` triggers 403 responses (unauthorized access to private pastes)

### Request Flow

1. **User Registration**: POST `/register` → `AuthController.register()` → `UserService.registerUser()` → Hash password → Save user → Redirect to login
2. **User Login**: POST `/login` → Spring Security Authentication → Success: Redirect to home
3. **Create Paste**:
   - POST `/api/pastes` → `PasteApiController.createPaste()` → `PasteService.createPaste()`
   - Extract authenticated user from SecurityContext (if logged in)
   - Set owner, visibility, and shared users
   - Save to DB → Return ID and URL
4. **View Paste**:
   - GET `/api/pastes/{id}` → `PasteApiController.getPaste()` → `PasteService.getPaste()`
   - Check expiration (time/views)
   - **Access Control**: If PRIVATE, verify user is owner or in sharedWith list
   - Increment view count → Return content
5. **Dashboard**:
   - GET `/dashboard` → `DashboardController.dashboard()`
   - Fetch owned pastes: `PasteService.getUserOwnedPastes()` → `PasteRepository.findByOwnerOrderByCreatedAtDesc()`
   - Fetch shared pastes: `PasteService.getUserSharedPastes()` → `PasteRepository.findBySharedWithContainingOrderByCreatedAtDesc()`
   - Display in dashboard template

## Authentication & Sharing Features

### User Management
- **Registration**: Users create accounts with username, email, and password
- **Login**: Session-based authentication via Spring Security
- **Password Security**: BCrypt hashing with default strength (10 rounds)

### Paste Visibility & Access Control
- **PUBLIC Pastes**: Accessible to anyone with the link (logged in or anonymous)
- **PRIVATE Pastes**: Only accessible to:
  - The paste owner
  - Users explicitly shared with via `sharedWith` list
  - Returns 403 Forbidden if unauthorized

### Sharing Workflow
1. User must be logged in to create PRIVATE pastes
2. Owner selects PRIVATE visibility
3. Owner selects one or more users from dropdown (multi-select)
4. Backend validates shared users exist and associates them
5. Shared users can access paste via link

### Database Schema
- **users** table: id, username, email, password, enabled, created_at
- **paste_shares** join table: paste_id, user_id (many-to-many)
- **paste** table: Added owner_id (FK to users), visibility (enum)

## Testing Notes

- Test mode: Set `TEST_MODE=1` to enable time injection for testing expiration
- Time injection: Use header `x-test-now-ms` with epoch milliseconds to simulate time
- Basic smoke test exists in `PastebinliteApplicationTests` (Spring context loading)
- **Testing Authentication**: Use Spring Security test utilities for authenticated requests

## Important Implementation Details

- **Lombok**: Used for entity boilerplate (`@Data`, `@NoArgsConstructor`). Requires annotation processor configuration in Maven.
- **NanoID**: Library included (`jnanoid`) but not currently used. Entity uses JPA UUID generation instead.
- **Transaction Management**: `@Transactional` on service methods ensures view count updates and user operations are atomic.
- **Security**:
  - XSS protection via Thymeleaf auto-escaping
  - SQL injection prevention via JPA parameterized queries
  - Password hashing with BCrypt (strength 10)
  - CSRF protection enabled for form submissions, disabled for REST API
  - Access control via Spring Security `SecurityContextHolder`
- **Authentication Flow**: Form-based login → Spring Security validates → Session created → SecurityContext populated
- **Access Control Pattern**: Service layer checks `SecurityContextHolder.getContext().getAuthentication()` for current user
- **Base URL Construction**: `PasteApiController` constructs URLs from `HttpServletRequest` (handles ports, schemes correctly).
- **Anonymous Pastes**: Users can create PUBLIC pastes without logging in (owner field remains null).

## User Dashboard

### Features
- **My Pastes**: Lists all pastes created by the logged-in user
  - Shows visibility (PUBLIC/PRIVATE), creation date, expiration, view counts
  - Displays number of users each private paste is shared with
  - Direct links to view each paste
- **Shared with Me**: Lists all private pastes shared with the logged-in user
  - Shows who shared the paste (owner)
  - Same metadata as owned pastes
  - Accessible at `/dashboard` (requires authentication)

### Implementation
- Repository methods: `findByOwnerOrderByCreatedAtDesc()` and `findBySharedWithContainingOrderByCreatedAtDesc()`
- Eager fetching on `owner` and `sharedWith` relationships prevents N+1 query issues
- Dashboard link visible in navigation when authenticated

## Configuration Files

- `application.properties` - Database, JPA, and Actuator configuration
- `pom.xml` - Maven dependencies (includes Spring Security, Thymeleaf Spring Security integration)
- Templates in `src/main/resources/templates/`:
  - `index.html` - Home page with paste creation (includes auth UI and sharing options)
  - `login.html` - Login page
  - `register.html` - Registration page
  - `dashboard.html` - User dashboard showing owned and shared pastes
  - `view-paste.html` - Paste viewing page
  - `404.html` - Error page
