# JulOS

## Overview

JulOS is a robust Spring Boot application designed to monitor and manage productivity in medical packaging operations. It provides real-time tracking, user performance metrics, and comprehensive reporting features for administrators and moderators.

## Features

- **Real-time Dashboard**: Live updates of productivity metrics using Server-Sent Events (SSE).
- **Role-Based Access Control**: Differentiated access for Checkers, Moderators, and Administrators.
- **Productivity Tracking**: Monitors total submissions, pouches checked, average time duration, and pouches per hour.
- **User Management**: Secure user registration and authentication system.
- **Responsive Design**: Mobile-friendly interface with a dark theme for comfortable viewing.
- **Data Visualization**: Clear and intuitive display of productivity statistics.
- **Form Validation**: Client-side and server-side validation for data integrity.
- **RESTful API**: Endpoints for retrieving productivity data and user information.
- **Database Integration**: Supports both H2 (for development) and PostgreSQL (for production) databases.

## Technology Stack

- **Backend**: Spring Boot, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, JavaScript, CSS3
- **Database**: H2 (dev), PostgreSQL (prod)
- **Build Tool**: Maven
- **Version Control**: Git

## Getting Started

### Prerequisites

- JDK 11 or later
- Maven 3.6+
- PostgreSQL (for production deployment)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/juliannoronha/mck
   ```

2. Navigate to the project directory:
   ```
   cd demoproject/demo
   ```

3. Build the project:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   java -jar target/demo-0.0.1-SNAPSHOT.jar
   ```

The application will be available at `http://localhost:8080`.

## Configuration

- Database configuration can be modified in `application.properties`.
- For production, ensure to set appropriate environment variables for database credentials and other sensitive information.

## Usage

- Access the main application at `/packmed`.
- Administrators can view all user productivity data at `/user-productivity`.
- API endpoints are available under `/api/*` for programmatic access to productivity data.

## Security

- The application implements CSRF protection and secure password hashing.
- All sensitive operations require authentication and proper authorization.

## Authors

- Julian Noronha -  [juliannoronha](https://github.com/juliannoronha)
