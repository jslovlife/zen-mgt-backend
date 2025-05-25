# Zen Management Backend API

A Spring Boot based backend API using Java 17 and Maven.

## Prerequisites

- JDK 17
- Maven 3.6+

## Project Structure

```
zen-mgt-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/zenmgt/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

## Getting Started

1. Clone the repository
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on port 8080 with context path `/api`.

## API Endpoints

- Health Check: `GET http://localhost:8080/api/health`

## Database

The application uses H2 in-memory database. You can access the H2 console at:
`http://localhost:8080/api/h2-console`

Database credentials:
- JDBC URL: `jdbc:h2:mem:zenmgtdb`
- Username: `sa`
- Password: `password`

## Development

The project uses:
- Spring Boot 3.2.3
- Spring Data JPA
- Spring Web
- H2 Database
- Lombok

## Logging

- Root logging level: INFO
- Application logging level: DEBUG

## Generate JWT Secret key 
- openssl rand -base64 64
