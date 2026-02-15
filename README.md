# TestMyDev - Spring Boot Sample Project

A sample Spring Boot application with Maven for testing and development purposes.

## Features

- **Spring Boot 3.2.2** with Java 17
- **REST API** endpoints
- **H2 In-Memory Database** for development
- **Spring Data JPA** for data persistence
- **Spring Boot Actuator** for monitoring
- **Maven** build system with wrapper

## Project Structure

```
testmydev/
├── .mvn/wrapper/                              # Maven wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/example/testmydev/
│   │   │   ├── TestmydevApplication.java      # Main application class
│   │   │   ├── controller/
│   │   │   │   └── HelloController.java       # REST controller
│   │   │   └── model/
│   │   │       └── User.java                  # JPA entity
│   │   └── resources/
│   │       └── application.properties         # Configuration
│   └── test/
│       └── java/com/example/testmydev/
│           └── TestmydevApplicationTests.java # Test class
├── mvnw                                       # Maven wrapper script (Unix)
├── mvnw.cmd                                   # Maven wrapper script (Windows)
├── pom.xml                                    # Maven configuration
├── .gitignore                                 # Git ignore rules
└── README.md                                  # This file
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Git

**Note:** Maven is not required as the project includes Maven Wrapper (`mvnw`).

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/devendra1985/testmydev.git
   cd testmydev
   ```

2. **Make the Maven wrapper executable (Unix/Mac):**
   ```bash
   chmod +x mvnw
   ```

3. **Clean and compile the project:**
   ```bash
   ./mvnw clean compile
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

   **Alternative for Windows:**
   ```cmd
   mvnw.cmd spring-boot:run
   ```

5. **Access the application:**
   - Application: http://localhost:8081/testmydev
   - H2 Console: http://localhost:8081/testmydev/h2-console
   - Health Check: http://localhost:8081/testmydev/actuator/health

### Troubleshooting Build Issues

If you encounter Maven plugin issues:

1. **Clear local Maven repository cache:**
   ```bash
   rm -rf ~/.m2/repository/org/springframework/boot/
   ```

2. **Use Maven wrapper instead of system Maven:**
   ```bash
   ./mvnw clean compile
   ./mvnw spring-boot:run
   ```

3. **Force update dependencies:**
   ```bash
   ./mvnw clean compile -U
   ```

4. **Port conflict (if port 8081 is also in use):**
   ```bash
   # Check what's using the port
   lsof -i :8081
   
   # Or run on a different port
   ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
   ```

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/testmydev/api/hello` | Simple hello message |
| GET | `/testmydev/api/hello/{name}` | Personalized hello message |
| GET | `/testmydev/api/health` | Application health status |
| GET | `/testmydev/actuator/health` | Spring Boot actuator health |

### Example API Calls

```bash
# Basic hello
curl http://localhost:8081/testmydev/api/hello

# Personalized hello
curl http://localhost:8081/testmydev/api/hello/John

# Health check
curl http://localhost:8081/testmydev/api/health
```

### Running Tests

```bash
./mvnw test
```

### Building JAR

```bash
./mvnw clean package
java -jar target/testmydev-0.0.1-SNAPSHOT.jar
```

## Database Configuration

The application uses H2 in-memory database by default:

- **URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** `password`
- **Console:** http://localhost:8081/testmydev/h2-console

## Development

This is a sample project for learning and testing Spring Boot features. Feel free to:

- Add new controllers and services
- Implement additional JPA entities
- Add more comprehensive tests
- Integrate with external databases
- Add security features

## Technologies Used

- Spring Boot 3.2.2
- Spring Web
- Spring Data JPA
- Spring Boot Actuator
- H2 Database
- Maven 3.9.6 (via wrapper)
- JUnit 5

## License

This project is for educational and testing purposes.