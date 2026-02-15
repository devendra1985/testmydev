# TestMyDev - Spring Boot Sample Project

A sample Spring Boot application with Maven for testing and development purposes.

## Features

- **Spring Boot 3.2.2** with Java 17
- **REST API** endpoints
- **H2 In-Memory Database** for development
- **Spring Data JPA** for data persistence
- **Spring Boot Actuator** for monitoring
- **Maven** build system

## Project Structure

```
testmydev/
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
├── pom.xml                                    # Maven configuration
└── README.md                                  # This file
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/devendra1985/testmydev.git
   cd testmydev
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application:**
   - Application: http://localhost:8080/testmydev
   - H2 Console: http://localhost:8080/testmydev/h2-console
   - Health Check: http://localhost:8080/testmydev/actuator/health

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
curl http://localhost:8080/testmydev/api/hello

# Personalized hello
curl http://localhost:8080/testmydev/api/hello/John

# Health check
curl http://localhost:8080/testmydev/api/health
```

### Running Tests

```bash
mvn test
```

### Building JAR

```bash
mvn clean package
java -jar target/testmydev-0.0.1-SNAPSHOT.jar
```

## Database Configuration

The application uses H2 in-memory database by default:

- **URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** `password`
- **Console:** http://localhost:8080/testmydev/h2-console

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
- Maven
- JUnit 5

## License

This project is for educational and testing purposes.