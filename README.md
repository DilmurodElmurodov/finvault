# FinVault Backend — Premium Finance Intelligence Platform

Spring Boot REST API with JWT Auth, PostgreSQL, Docker

## Tech Stack
- **Java 17** + Spring Boot 3.2
- **PostgreSQL** (Flyway migrations)
- **Spring Security** + JWT
- **Swagger/OpenAPI** documentation
- **Docker** + Nginx
- **Maven** build system

## Project Structure
```
finvault/
├── src/main/java/com/finvault/
│   ├── config/          # Security, OpenAPI, CORS
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/Response DTOs
│   │   ├── request/
│   │   └── response/
│   ├── entity/          # JPA entities
│   ├── exception/       # Custom exceptions & global handler
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/jwt/    # JWT filter and utilities
│   └── service/         # Business logic
│       └── impl/
├── src/main/resources/
│   ├── db/migration/    # Flyway SQL migrations
│   └── application.yml
├── docker/
│   ├── docker-compose.yml
│   └── nginx.conf
└── Dockerfile
```

## Quick Start (Local Development)

### 1. Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or use Docker)
- IntelliJ IDEA (recommended)

### 2. Start PostgreSQL with Docker
```bash
cd docker
docker-compose up postgres -d
```

### 3. Configure application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finvault_db
    username: finvault
    password: finvault_pass
```

### 4. Run the application
```bash
# Maven
mvn spring-boot:run

# Or in IntelliJ: right-click FinVaultApplication.java → Run
```

### 5. Access
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/api-docs

## API Endpoints

### Authentication
```
POST /api/auth/register       → Register new user
POST /api/auth/login          → Login, get JWT tokens
POST /api/auth/refresh-token  → Refresh access token
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET  /api/auth/verify-email
```

### Portfolio
```
GET    /api/portfolios         → List all portfolios
POST   /api/portfolios         → Create portfolio
GET    /api/portfolios/{id}    → Get portfolio
PUT    /api/portfolios/{id}    → Update portfolio
DELETE /api/portfolios/{id}    → Delete portfolio
```

### Financial Calculators
```
POST /api/calculator/emi               → EMI calculation
POST /api/calculator/compound-interest → Compound interest
POST /api/calculator/roi               → Return on investment
POST /api/calculator/sip               → SIP calculation
```

### Admin (ADMIN role)
```
GET   /api/admin/users               → All users
PATCH /api/admin/users/{id}/toggle-active
PATCH /api/admin/users/{id}/role
GET   /api/admin/stats               → Platform analytics
```

## Authentication Flow

1. Register: `POST /auth/register` → get `accessToken` + `refreshToken`
2. Use `Authorization: Bearer <accessToken>` header on all protected requests
3. Refresh: `POST /auth/refresh-token` with `X-Refresh-Token: <refreshToken>`

## IntelliJ Setup
1. File → Open → select `finvault/` folder
2. IntelliJ auto-detects Maven project
3. Add PostgreSQL in Database panel (localhost:5432/finvault_db)
4. Run `FinVaultApplication.java`

## Deploy to VPS (Linux)

```bash
# Build JAR
mvn clean package -DskipTests

# Copy to server
scp target/finvault-backend-1.0.0.jar user@your-server:/opt/finvault/

# Run with systemd (create /etc/systemd/system/finvault.service)
[Unit]
Description=FinVault Backend
After=network.target postgresql.service

[Service]
User=finvault
WorkingDirectory=/opt/finvault
ExecStart=/usr/bin/java -jar finvault-backend-1.0.0.jar
Restart=always
Environment="DB_USERNAME=finvault"
Environment="DB_PASSWORD=your_secure_pass"
Environment="JWT_SECRET=your_256bit_secret"

[Install]
WantedBy=multi-user.target

# Enable and start
sudo systemctl enable finvault
sudo systemctl start finvault
```

## Or with Docker Compose
```bash
docker-compose -f docker/docker-compose.yml up -d
```

## Security Best Practices
- JWT secret: use 256-bit random string (`openssl rand -hex 32`)
- Passwords: BCrypt with cost factor 12
- HTTPS: TLS 1.2/1.3 via Nginx
- Rate limiting: 60 req/min per IP
- CORS: configured per environment
- Input validation: Bean Validation on all DTOs
- SQL injection: prevented by JPA/Hibernate parameterized queries
- XSS: security headers via Nginx

## Default Admin Account
- Email: `admin@finvault.io`
- Password: `Admin@1234` ← Change immediately in production!

## Environment Variables
```
DB_USERNAME=finvault
DB_PASSWORD=your_db_password
JWT_SECRET=your_256_bit_secret
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=noreply@yourdomain.com
MAIL_PASSWORD=your_app_password
FILE_UPLOAD_DIR=/opt/finvault/uploads
```
