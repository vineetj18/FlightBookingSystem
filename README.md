# Flight Booking System

A production-ready Flight Booking System built with Spring Boot, featuring Redis distributed locking, message queues, and payment integration.

## 🚀 Features

- **Flight Management**: Add, search, and manage flights
- **Sequential Seat Assignment**: Automatic seat assignment (A1, A2, A3, etc.)
- **Multiple Seats per Booking**: Support for booking multiple seats in one transaction
- **Distributed Locking**: Redis-based seat locking to prevent overbooking
- **Message Queues**: Asynchronous seat creation processing
- **Payment Integration**: Third-party payment gateway integration
- **Security**: Role-based access control
- **Monitoring**: Health checks and metrics

## 🏗️ Architecture

### High-Level Design
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Flight API    │    │  Search API     │    │  Booking API    │
│   (Admin)       │    │  (Public)       │    │  (User)         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Application Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │Flight Service│  │Search Service│  │Booking Service│           │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Layer                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │Flight DB    │  │Seats DB     │  │Bookings DB  │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │Message Queue│  │Redis Cache  │  │Payment API  │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

### Database Schema

#### Flights Table
- `id` (Primary Key)
- `flight_number` (Non-unique - same flight can run on multiple days)
- `from_location`
- `to_location`
- `flight_metadata`
- `departure_time` (LocalDateTime)
- `arrival_time` (LocalDateTime)
- `status` (SCHEDULED, ON_TIME, DELAYED, CANCELLED, DEPARTED, ARRIVED)
- `price`
- `max_passengers`
- `available_seats`
- `created_at`
- `updated_at`

#### Seats Table
- `id` (Primary Key)
- `flight_id` (Foreign Key)
- `seat_id` (A1, A2, B1, B2, etc.)
- `status` (AVAILABLE, OCCUPIED, LOCKED, MAINTENANCE)
- `created_at`
- `updated_at`

#### Bookings Table
- `id` (Primary Key)
- `booking_id` (Unique)
- `flight_id` (Foreign Key)
- `booked_by`
- `pax_details` (JSON)
- `number_of_passengers` (Integer)
- `total_price` (BigDecimal)
- `payment_id`
- `status` (PENDING, CONFIRMED, CANCELLED, FAILED, REFUNDED)
- `payment_status` (PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED)
- `pnr` (Unique)
- `created_at`
- `updated_at`

#### Booking Seats Table
- `id` (Primary Key)
- `booking_id` (Foreign Key)
- `seat_id` (A1, A2, etc.)
- `passenger_name`
- `seat_price`
- `created_at`
- `updated_at`

## 🛠️ Technology Stack

- **Java 8**
- **Spring Boot 2.7.18**
- **Spring Data JPA**
- **Spring Security**
- **Spring AMQP (RabbitMQ)**
- **Redis**
- **H2 Database** (Development)
- **PostgreSQL** (Production)
- **Maven**
- **Docker** (Optional)

## 📋 Prerequisites

### Required Software
- **Java 8** (JDK 1.8)
- **Maven 3.6+**
- **Git**

### Optional Dependencies (for Production)
- **Redis Server** (for distributed locking)
- **RabbitMQ Server** (for message queuing)
- **PostgreSQL** (for production database)

## 🚀 Quick Start Guide

### Step 1: Install Prerequisites

#### Install Java 8:
```bash
# On macOS (using Homebrew)
brew install openjdk@8

# On Ubuntu/Debian
sudo apt update
sudo apt install openjdk-8-jdk

# On Windows
# Download from: https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html
```

#### Install Maven:
```bash
# On macOS (using Homebrew)
brew install maven

# On Ubuntu/Debian
sudo apt install maven

# On Windows
# Download from: https://maven.apache.org/download.cgi
```

#### Install Git:
```bash
# On macOS (using Homebrew)
brew install git

# On Ubuntu/Debian
sudo apt install git

# On Windows
# Download from: https://git-scm.com/download/win
```

### Step 2: Get the Project

#### Option A: Clone from Git (if you have a repository)
```bash
git clone <your-repository-url>
cd flight-booking-system
```

#### Option B: Copy the Project Files
```bash
# Create project directory
mkdir flight-booking-system
cd flight-booking-system

# Copy all the project files from your current setup
# (Copy the entire project folder to the new laptop)
```

### Step 3: Build and Run

#### 1. Navigate to Project Directory:
```bash
cd flight-booking-system
```

#### 2. Clean and Build:
```bash
mvn clean compile
```

#### 3. Package the Application:
```bash
mvn clean package -DskipTests
```

#### 4. Run the Application:
```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using JAR file
java -jar target/flight-booking-system-1.0-SNAPSHOT.jar
```

### Step 4: Verify Application is Running

#### Check Health:
```bash
curl http://localhost:8080/actuator/health
```

#### Expected Response:
```json
{
  "status": "UP"
}
```

## 📚 API Documentation

### Base URL
- **API Base URL**: http://localhost:8080/api/v1
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

### Flight Management APIs

#### 1. Add Flight (Admin Only)
```bash
curl -X POST http://localhost:8080/api/v1/flights/admin/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "flightNumber": "FL001",
    "from": "New York",
    "to": "Los Angeles",
    "flightMetadata": "Boeing 737",
    "departureTime": "2025-12-25T10:00:00",
    "arrivalTime": "2025-12-25T13:00:00",
    "price": 299.99,
    "maxPassengers": 150
  }'
```

#### 2. Search Flights (Public)
```bash
curl -X POST http://localhost:8080/api/v1/flights/search \
  -H "Content-Type: application/json" \
  -d '{
    "from": "New York",
    "to": "Los Angeles",
    "passengers": 2,
    "date": "2025-12-25T00:00:00"
  }'
```

### Booking APIs

#### 3. Create Booking (User)
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user-token" \
  -d '{
    "flightId": 1,
    "numberOfPassengers": 2,
    "paxDetails": "John Doe, Jane Doe",
    "bookedBy": "user@example.com"
  }'
```

#### 4. Get Booking by ID
```bash
curl -X GET http://localhost:8080/api/v1/bookings/{bookingId} \
  -H "Authorization: Bearer user-token"
```

#### 5. Cancel Booking
```bash
curl -X PUT http://localhost:8080/api/v1/bookings/{bookingId}/cancel \
  -H "Authorization: Bearer user-token"
```

## 🔒 Security

The system implements role-based access control:

- **ADMIN**: Can add/update/delete flights
- **USER**: Can search flights and create bookings
- **Public**: Can search flights

### Authentication Tokens
- **Admin Token**: `admin-token`
- **User Token**: `user-token`

## 🔄 Key Features

### Sequential Seat Assignment
- Seats are assigned sequentially (A1, A2, A3, etc.)
- System automatically finds next available seats
- Prevents seat conflicts during booking

### Multiple Seats per Booking
- Single booking can contain multiple seats
- All seats in a booking have the same price
- Atomic locking for all selected seats

### Distributed Locking
The system uses Redis for distributed locking to prevent race conditions:

- **Seat Locking**: 10-minute TTL with automatic release
- **Atomic Operations**: Lua scripts for atomic lock operations
- **Lock Extension**: Automatic lock renewal during booking process

## 🐳 Docker Setup (Optional)

### Create docker-compose.yml:
```yaml
version: '3.8'
services:
  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
  
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin
  
  postgres:
    image: postgres:13
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: flightdb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
```

### Run with Docker:
```bash
# Start dependencies
docker-compose up -d

# Run the application
mvn spring-boot:run
```

## 🔧 Configuration

### Application Properties
```yaml
# Database (H2 for development)
spring:
  datasource:
    url: jdbc:h2:mem:flightdb
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true

# Redis (for distributed locking)
  redis:
    host: localhost
    port: 6379

# RabbitMQ (for message queuing)
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# Security
  security:
    user:
      name: admin
      password: admin
```

### Production Configuration
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/flightdb
    username: admin
    password: admin
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
```

## 📊 Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: `/actuator/health/db`
- **Redis Health**: `/actuator/health/redis`

### Metrics
- **Application Metrics**: `/actuator/metrics`
- **Custom Metrics**: Booking success rate, payment failure rate

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Test Categories
- **Unit Tests**: Service layer testing
- **Integration Tests**: API endpoint testing
- **Contract Tests**: Message queue testing

## 🚀 Production Deployment

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/flightdb
export REDIS_URL=redis://localhost:6379
export RABBITMQ_URL=amqp://localhost:5672
```

### Docker Deployment
```bash
# Build image
docker build -t flight-booking-system .

# Run with docker-compose
docker-compose up -d
```

## 📈 Performance Considerations

### Caching Strategy
- **Flight Search Results**: Cached for 5 minutes
- **Available Seats**: Real-time with Redis locks
- **User Sessions**: Stateless with JWT tokens

### Database Optimization
- **Indexes**: On frequently queried columns
- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: JPA query optimization

### Scalability
- **Horizontal Scaling**: Stateless application design
- **Load Balancing**: Multiple application instances
- **Database Sharding**: By flight date or region

## 🐛 Troubleshooting

### Common Issues

1. **Java Version Issues**
   ```bash
   # Check Java version
   java -version
   # Should show Java 8
   ```

2. **Maven Build Issues**
   ```bash
   # Clean and rebuild
   mvn clean compile
   ```

3. **Application Won't Start**
   ```bash
   # Check logs
   tail -f logs/application.log
   ```

4. **Database Connection Issues**
   ```bash
   # Check H2 console
   # Go to http://localhost:8080/h2-console
   # JDBC URL: jdbc:h2:mem:flightdb
   # Username: sa
   # Password: (empty)
   ```

### Logs
```bash
# Application logs
tail -f logs/application.log

# Specific service logs
grep "BookingService" logs/application.log
```

## 📋 Complete API Testing Commands

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Add Flight
```bash
curl -X POST http://localhost:8080/api/v1/flights/admin/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-token" \
  -d '{
    "flightNumber": "FL001",
    "from": "New York",
    "to": "Los Angeles",
    "flightMetadata": "Boeing 737",
    "departureTime": "2024-12-25T10:00:00",
    "arrivalTime": "2024-12-25T13:00:00",
    "price": 299.99,
    "maxPassengers": 150
  }'
```

### Search Flights
```bash
curl -X POST http://localhost:8080/api/v1/flights/search \
  -H "Content-Type: application/json" \
  -d '{
    "from": "New York",
    "to": "Los Angeles",
    "passengers": 2,
    "date": "2024-12-25T00:00:00"
  }'
```

### Create Booking
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer user-token" \
  -d '{
    "flightId": 1,
    "numberOfPassengers": 2,
    "paxDetails": "John Doe, Jane Doe",
    "bookedBy": "user@example.com"
  }'
```

## 🎯 Key Features Working

- ✅ **Sequential Seat Assignment** (A1, A2, A3, etc.)
- ✅ **Multiple Seats per Booking**
- ✅ **Redis Distributed Locking**
- ✅ **Payment Gateway Integration**
- ✅ **Database Schema with Relationships**
- ✅ **Complete REST API**
- ✅ **Error Handling**
- ✅ **Security Configuration**

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- **Email**: support@flightbooking.com
- **Documentation**: [API Docs](http://localhost:8080/swagger-ui.html)
- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)