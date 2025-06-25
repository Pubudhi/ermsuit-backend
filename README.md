# ERMSuit - Enterprise Report Management System

ERMSuit is a comprehensive system for enterprise users to upload data, generate professional PDF reports with charts, schedule automated reports, manage users and roles, and monitor all actions.

## Features

- **User Management**: Signup, login with JWT authentication, admin/user roles
- **Report Data API**: Upload CSV/JSON data, store in database, validate input
- **PDF Report Generator**: Generate and export PDF with charts using Aspose
- **Scheduled Report Jobs**: Schedule daily/weekly/monthly reports using cron expressions
- **Audit & Logs**: Track user activity and job runs

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Web, Spring Security
- **PDF/Charting**: Aspose.PDF, Aspose.Cells
- **Database**: H2 (development), PostgreSQL (production)
- **Authentication**: JWT
- **Monitoring**: Spring Boot Actuator

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL (for production)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yourusername/ERMSuit.git
cd ERMSuit
```

### Build the Application

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### H2 Console

During development, you can access the H2 database console at:

```
http://localhost:8080/h2-console
```

Use the following credentials:
- JDBC URL: `jdbc:h2:mem:ermsuit`
- Username: `sa`
- Password: `password`

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Users

- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users (admin only)

### Data Sources

- `POST /api/data-sources` - Upload a new data source
- `GET /api/data-sources/{id}` - Get data source by ID
- `GET /api/data-sources` - Get all data sources
- `GET /api/data-sources/my-data-sources` - Get current user's data sources

### Report Templates

- `POST /api/report-templates` - Create a new report template (admin only)
- `GET /api/report-templates/{id}` - Get template by ID
- `GET /api/report-templates` - Get all templates
- `GET /api/report-templates/by-type/{type}` - Get templates by type

### Reports

- `POST /api/reports` - Generate a new report
- `GET /api/reports/{id}` - Get report by ID
- `GET /api/reports` - Get all reports
- `GET /api/reports/my-reports` - Get current user's reports
- `GET /api/reports/{id}/download` - Download report PDF

### Report Schedules

- `POST /api/report-schedules` - Create a new report schedule
- `GET /api/report-schedules/{id}` - Get schedule by ID
- `GET /api/report-schedules` - Get all schedules
- `GET /api/report-schedules/my-schedules` - Get current user's schedules
- `PATCH /api/report-schedules/{id}/active` - Toggle schedule active status

### Admin

- `POST /api/admin/users` - Create an admin user
- `GET /api/admin/audit-logs` - Get audit logs
- `GET /api/admin/audit-logs/entity/{entityType}/{entityId}` - Get audit logs by entity

## Configuration

The application can be configured through the `application.yml` file. Key configuration options include:

- Database connection
- JWT secret and expiration
- File storage locations
- Logging levels

## Production Deployment

For production deployment:

1. Update the database configuration to use PostgreSQL
2. Set a secure JWT secret
3. Configure proper file storage paths
4. Set up proper logging

## License

This project is licensed under the MIT License - see the LICENSE file for details.
