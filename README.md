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

USER can login, view resources, create reservations, view own reservations, and view own reservation by ID.

USER cannot manage resources, view all reservations, update/delete reservations, or access another user's reservation.

### ADMIN

ADMIN can manage users and resources, create reservations, view all reservations, view any reservation, update reservations, and delete reservations.

## Base URL

```text
http://localhost:8080
Authentication
Login
POST /auth/login

Example:

{
  "username": "admin",
  "password": "your_password"
}

Use the returned token for protected APIs:

Authorization: Bearer <JWT_TOKEN>
Resource APIs
Method	Endpoint	Access
GET	/api/resources	USER, ADMIN
GET	/api/resources/{id}	USER, ADMIN
POST	/api/resources	ADMIN
PUT	/api/resources/{id}	ADMIN
DELETE	/api/resources/{id}	ADMIN
Example Resource
{
  "name": "Conference Room",
  "description": "Meeting room with projector and WiFi",
  "price": 1500.00,
  "available": true
}
Reservation APIs
Method	Endpoint	Access
POST	/api/reservations	USER, ADMIN
GET	/api/reservations/my	USER, ADMIN
GET	/api/reservations	ADMIN
GET	/api/reservations/{id}	USER own, ADMIN any
PUT	/api/reservations/{id}	ADMIN
DELETE	/api/reservations/{id}	ADMIN
Example Reservation
{
  "resourceId": 1,
  "startTime": "2026-09-28T14:00:00",
  "endTime": "2026-09-28T16:00:00"
}

The user ID is taken from the JWT, not the request body.

The reservation price is taken from the selected resource.

Filtering, Pagination and Sorting
Supported Filters
GET /api/reservations?status=PENDING
GET /api/reservations?minPrice=500
GET /api/reservations?maxPrice=2000
GET /api/reservations?status=PENDING&minPrice=500&maxPrice=2000
Pagination and Sorting
GET /api/reservations?page=0&size=10&sort=price,desc

Defaults:

page=0
size=10
sort=id,asc
Validation and Conflict Handling

The application checks:

Required fields
Valid resource IDs
Resource availability
startTime must be before endTime

Overlapping reservations for the same resource are rejected.

Example:

Existing: 10:00 - 12:00
New:      11:00 - 13:00
Response: 409 Conflict

Message:

Resource is already booked for the selected time
Error Handling

Common responses:

400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
Database Setup

Create the database:

CREATE DATABASE resource_booking;

Configure:

src/main/resources/application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking
spring.datasource.username=root
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update

app.jwt.secret=your_long_jwt_secret
app.jwt.expiration=86400000

Do not commit real database passwords or JWT secrets to a public repository.

Project Structure
src/main/java/com/example/resourcebooking

├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── security
└── service
Architecture
Client / Postman
       |
       v
Controller
       |
       v
Service
       |
       v
Repository
       |
       v
MySQL

Security flow:

Login
  |
  v
JWT
  |
  v
JwtAuthenticationFilter
  |
  v
Spring Security
  |
  v
Role / Ownership Check
  |
  v
Controller
Run the Application
Create the resource_booking MySQL database.
Configure MySQL and JWT settings.
Open the project in Eclipse or IntelliJ IDEA.
Run ResourceBookingApplication.java.
Open http://localhost:8080.
Testing

The APIs were tested with Postman for:

ADMIN and USER login
JWT authentication
Resource CRUD
Reservation create/read/update/delete
Status and price filters
Pagination and sorting
Validation errors
USER/ADMIN authorization
Reservation ownership
Reservation conflict handling
API Access Summary
Endpoint	USER	ADMIN
POST /auth/login	Yes	Yes
GET /api/resources	Yes	Yes
POST /api/resources	No	Yes
PUT /api/resources/{id}	No	Yes
DELETE /api/resources/{id}	No	Yes
POST /api/reservations	Yes	Yes
GET /api/reservations/my	Yes	Yes
GET /api/reservations	No	Yes
GET /api/reservations/{id}	Own only	Any
PUT /api/reservations/{id}	No	Yes
DELETE /api/reservations/{id}	No	Yes
Author

Shivam Raut

Backend Developer Assignment - Resource Booking System.
