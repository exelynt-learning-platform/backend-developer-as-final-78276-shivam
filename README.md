# Resource Booking System

## Backend Developer Assignment

Secure RESTful Resource Booking System built with Java 17, Spring Boot 4.1.1, Spring Security, JWT, Spring Data JPA, Hibernate, MySQL, Maven, Lombok, and Postman.

## Features

- JWT authentication and stateless security
- ADMIN and USER role-based access control
- BCrypt password encryption
- Resource CRUD
- Reservation creation and management
- Reservation ownership validation
- PENDING, CONFIRMED, and CANCELLED statuses
- Status and price filtering
- Pagination and sorting
- Request validation
- Reservation overlap detection
- Centralized exception handling

## Roles and Permissions

### USER

USER can login, view resources, create reservations, view own reservations, and view own reservation by ID. USER cannot manage resources, view all reservations, update/delete reservations, or access another user's reservation.

### ADMIN

ADMIN can manage users and resources, create reservations, view all reservations, view any reservation, update reservations, and delete reservations.

## Base URL

```text
http://localhost:8080
```

## Authentication

### Login

```http
POST /auth/login
```

Example:

```json
{
  "username": "admin",
  "password": "your_password"
}
```

Use the returned token for protected APIs:

```text
Authorization: Bearer <JWT_TOKEN>
```

## Resource APIs

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/resources` | USER, ADMIN |
| GET | `/api/resources/{id}` | USER, ADMIN |
| POST | `/api/resources` | ADMIN |
| PUT | `/api/resources/{id}` | ADMIN |
| DELETE | `/api/resources/{id}` | ADMIN |

Example resource:

```json
{
  "name": "Conference Room",
  "description": "Meeting room with projector and WiFi",
  "price": 1500.00,
  "available": true
}
```

## Reservation APIs

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/reservations` | USER, ADMIN |
| GET | `/api/reservations/my` | USER, ADMIN |
| GET | `/api/reservations` | ADMIN |
| GET | `/api/reservations/{id}` | USER own, ADMIN any |
| PUT | `/api/reservations/{id}` | ADMIN |
| DELETE | `/api/reservations/{id}` | ADMIN |

Example reservation:

```json
{
  "resourceId": 1,
  "startTime": "2026-09-28T14:00:00",
  "endTime": "2026-09-28T16:00:00"
}
```

The user ID is taken from the JWT, not the request body. The reservation price is taken from the selected resource.

## Filtering, Pagination and Sorting

Supported filters:

```http
GET /api/reservations?status=PENDING
GET /api/reservations?minPrice=500
GET /api/reservations?maxPrice=2000
GET /api/reservations?status=PENDING&minPrice=500&maxPrice=2000
```

Pagination and sorting:

```http
GET /api/reservations?page=0&size=10&sort=price,desc
```

Defaults: `page=0`, `size=10`, `sort=id,asc`.

## Validation and Conflict Handling

The application checks required fields, valid resource IDs, resource availability, and that `startTime` is before `endTime`.

Overlapping reservations for the same resource are rejected. Example:

```text
Existing: 10:00 - 12:00
New:      11:00 - 13:00
Response: 409 Conflict
```

Message: `Resource is already booked for the selected time`

## Error Handling

Common responses:

400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict


## Database Setup

Create the database:

 sql
CREATE DATABASE resource_booking;
Configure `src/main/resources/application.properties`:
spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
app.jwt.secret=your_long_jwt_secret
app.jwt.expiration=86400000

## Project Structure

src/main/java/com/example/resourcebooking
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
└── service


## Architecture

```text
Client / Postman -> Controller -> Service -> Repository -> MySQL

Login -> JWT -> JwtAuthenticationFilter -> Spring Security -> Role/Ownership Check
```

## Run the Application

1. Create the `resource_booking` MySQL database.
2. Configure MySQL and JWT settings.
3. Open the project in Eclipse or IntelliJ IDEA.
4. Run `ResourceBookingApplication.java`.
5. Open `http://localhost:8080`.

## Testing

The APIs were tested with Postman for:

- ADMIN and USER login
- JWT authentication
- Resource CRUD
- Reservation create/read/update/delete
- Status and price filters
- Pagination and sorting
- Validation errors
- USER/ADMIN authorization
- Reservation ownership
- Reservation conflict handling

## Author

**Shivam Raut**

Backend Developer Assignment - Resource Booking System
