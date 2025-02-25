# IT Support Ticket System

A simple ticket management application that allows employees to report and track IT issues.

## Project Structure

```
ticketsystem/
├── backend/                  # Spring Boot backend application
│   ├── src/                  # Source code
│   ├── Dockerfile            # Docker configuration for backend
│   └── pom.xml               # Maven configuration for backend
├── swing-ui/                 # Swing client application
│   ├── src/                  # Source code
│   └── pom.xml               # Maven configuration for client
├── docker-compose.yml        # Docker Compose configuration
├── run.sh                    # Build and run script
└── README.md                 # This file
```

## Prerequisites

- Java 17 or later
- Docker and Docker Compose
- Maven
- Git

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/ticketsystem.git
cd ticketsystem
```

### 2. Configure Oracle Container Registry Access

You need to accept the Oracle license agreement and authenticate with the Oracle Container Registry to pull the Oracle XE image:

1. Visit [Oracle Container Registry](https://container-registry.oracle.com)
2. Sign in with your Oracle account (create one if needed)
3. Navigate to the Database section and accept the license agreement for the Express Edition
4. Login to the Oracle Container Registry via Docker:
   ```bash
   docker login container-registry.oracle.com
   ```

### 3. Build and Run the Application

#### Option 1: Using the provided script

```bash
chmod +x run.sh
./run.sh
```

#### Option 2: Manual setup

1. **Build the project**

```bash
mvn clean package -DskipTests
```

2. **Start Docker containers**

```bash
docker-compose up -d
```

3. **Run the Swing Client**

```bash
java -jar swing-ui/target/swing-client-1.0-SNAPSHOT.jar
```

## Access Points

- **Backend API**: http://localhost:8080/api
- **Swagger Documentation**: http://localhost:8080/swagger-ui.html
- **Swing Client**: Automatically launched via the JAR file

## Docker Containers

- **Backend Container**: ticket-backend
- **Database Container**: oracle-db

### Database Connection Details

- **JDBC URL**: jdbc:oracle:thin:@localhost:1521:XE
- **Username**: system
- **Password**: password

## Troubleshooting

### Oracle Database Issues

If the Oracle database fails to start:

```bash
# Check container logs
docker logs oracle-db

# Recreate the container
docker-compose down -v
docker-compose up -d
```

### Backend Connection Issues

```bash
# Restart the backend container
docker-compose restart ticket-backend

# Check logs
docker logs ticket-backend
```

### Swing Client Issues

If the Swing client fails to connect to the backend:

1. Ensure the backend is running: `http://localhost:8080/api/health`
2. Check the client logs in the console

## Backend Development

The backend is built with Spring Boot and includes:

- Spring Security for authentication
- Spring Data JPA for database interactions with auto-schema generation
- OpenAPI/Swagger for API documentation
- MapStruct for object mapping
- Lombok for reducing boilerplate code

## Client Development

The Swing client uses:

- MigLayout for UI layout
- Spring Web for API calls
- Jackson for JSON processing
- SwingX for enhanced components

## Building the Client JAR Manually

```bash
cd swing-ui
mvn clean package
```

The executable JAR will be created at `swing-ui/target/swing-client-1.0-SNAPSHOT.jar`