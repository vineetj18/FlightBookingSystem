# Flight Booking System

A production-ready Flight Booking System built with Spring Boot, featuring Redis distributed locking, message queues, and payment integration.

## 🚀 Features

- **Flight Management**: Add, search, and manage flights
- **Seat Management**: Automatic seat creation with Redis locking
- **Booking System**: Secure booking with payment integration
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
- `flight_number` (Unique)
- `from_location`
- `to_location`
- `flight_metadata`
- `departure_date`
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
- `price`
- `payment_id`
- `status` (PENDING, CONFIRMED, CANCELLED, FAILED, REFUNDED)
- `payment_status` (PENDING, SUCCESS, FAILED, REFUNDED, CANCELLED)
- `pnr` (Unique)
- `seat_id`
- `created_at`
- `updated_at`

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security**
- **Spring AMQP (RabbitMQ)**
- **Redis**
- **H2 Database** (Development)
- **PostgreSQL** (Production)
- **Maven**
- **Docker** (Optional)

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Redis Server
- RabbitMQ Server
- PostgreSQL (for production)

## 🚀 Quick Start

### 1. Clone and Setup
```bash
git clone <repository-url>
cd flight-booking-system
```

### 2. Start Dependencies
```bash
# Start Redis
redis-server

# Start RabbitMQ
rabbitmq-server
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

### 4. Access the Application
- **API Base URL**: http://localhost:8080/api/v1
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## 📚 API Documentation

### Flight Management APIs

#### Add Flight (Admin Only)
```http
POST /api/v1/flights/admin/add
Content-Type: application/json
Authorization: Bearer <admin-token>

{
  "flightNumber": "FL001",
  "from": "New York",
  "to": "Los Angeles",
  "flightMetadata": "Boeing 737",
  "departureDate": "2024-12-25T10:00:00",
  "price": 299.99,
  "maxPassengers": 150
}
```

#### Search Flights
```http
POST /api/v1/flights/search
Content-Type: application/json

{
  "from": "New York",
  "to": "Los Angeles",
  "passengers": 2,
  "date": "2024-12-25T00:00:00"
}
```

### Booking APIs

#### Create Booking
```http
POST /api/v1/bookings
Content-Type: application/json
Authorization: Bearer <user-token>

{
  "flightId": 1,
  "price": 299.99,
  "paxDetails": "John Doe, Jane Doe",
  "bookedBy": "user@example.com",
  "seatId": "A1"
}
```

#### Get Booking by ID
```http
GET /api/v1/bookings/{bookingId}
Authorization: Bearer <user-token>
```

#### Cancel Booking
```http
PUT /api/v1/bookings/{bookingId}/cancel
Authorization: Bearer <user-token>
```

## 🔒 Security

The system implements role-based access control:

- **ADMIN**: Can add/update/delete flights
- **USER**: Can search flights and create bookings
- **Public**: Can search flights

## 🔄 Distributed Locking

The system uses Redis for distributed locking to prevent race conditions:

- **Seat Locking**: 10-minute TTL with automatic release
- **Atomic Operations**: Lua scripts for atomic lock operations
- **Lock Extension**: Automatic lock renewal during booking process

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

## 🔧 Configuration

### Application Properties
```yaml
# Database
spring.datasource.url: jdbc:postgresql://localhost:5432/flightdb
spring.datasource.username: ${DB_USERNAME}
spring.datasource.password: ${DB_PASSWORD}

# Redis
spring.data.redis.host: ${REDIS_HOST:localhost}
spring.data.redis.port: ${REDIS_PORT:6379}

# RabbitMQ
spring.rabbitmq.host: ${RABBITMQ_HOST:localhost}
spring.rabbitmq.port: ${RABBITMQ_PORT:5672}

# Custom Properties
app.redis.lock-ttl: 600000
app.payment.gateway-url: ${PAYMENT_GATEWAY_URL}
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

1. **Redis Connection Failed**
   ```bash
   # Check Redis is running
   redis-cli ping
   ```

2. **RabbitMQ Connection Failed**
   ```bash
   # Check RabbitMQ is running
   rabbitmqctl status
   ```

3. **Database Connection Issues**
   ```bash
   # Check database connectivity
   psql -h localhost -U username -d flightdb
   ```

### Logs
```bash
# Application logs
tail -f logs/application.log

# Specific service logs
grep "BookingService" logs/application.log
```

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
