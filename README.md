# Smart Project and Collaboration Platform

A modern, full-stack enterprise-grade project management and collaboration system built with Spring Boot, Angular, and PostgreSQL.

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen)
![Angular](https://img.shields.io/badge/Angular-17-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

## 🚀 Features

### Core Functionality
- **User Authentication & Authorization**: Secure JWT-based authentication with role-based access control
- **Project Management**: Create, update, and manage projects with detailed information
- **Task Management**: Comprehensive task tracking with priorities, status, and due dates
- **Kanban Board**: Interactive drag-and-drop task board for visual task management
- **Activity Logging**: Track all user activities and changes in the system
- **RESTful APIs**: Well-documented APIs with Swagger/OpenAPI integration

### Technical Highlights
- **Responsive Design**: Modern Material UI with responsive layouts
- **Real-time Updates**: Asynchronous operations for better performance
- **Database Migration**: Flyway integration for version-controlled database schemas
- **API Documentation**: Interactive Swagger UI for easy API exploration
- **Health Monitoring**: Spring Boot Actuator endpoints for application health checks
- **Docker Support**: Complete containerization with Docker Compose

## 🏗️ Architecture

### System Architecture
```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│                 │      │                  │      │                 │
│  Angular 17     │◄────►│  Spring Boot     │◄────►│  PostgreSQL 16  │
│  Frontend       │ HTTP │  Backend API     │ JDBC │  Database       │
│  (Port 80)      │      │  (Port 8080)     │      │  (Port 5432)    │
│                 │      │                  │      │                 │
└─────────────────┘      └──────────────────┘      └─────────────────┘
        │                         │
        │                         │
        ▼                         ▼
   Material UI            Spring Security
   RxJS                   JWT Authentication
   Angular Router         JPA/Hibernate
   CDK Drag-Drop          Flyway Migration
```

### Backend Architecture (Spring Boot)

```
Controller Layer
    ├── AuthController (Authentication endpoints)
    ├── ProjectController (Project CRUD operations)
    └── TaskController (Task CRUD operations)
         ↓
Service Layer
    ├── ActivityLogService (Async activity logging)
    ├── ProjectService (Business logic for projects)
    └── TaskService (Business logic for tasks)
         ↓
Repository Layer (Spring Data JPA)
    ├── UserRepository
    ├── ProjectRepository
    ├── TaskRepository
    └── ActivityLogRepository
         ↓
Database (PostgreSQL)
```

### Frontend Architecture (Angular)

```
Core Module
    ├── AuthGuard (Route protection)
    ├── AuthInterceptor (JWT token injection)
    ├── AuthService (Authentication logic)
    └── MainLayoutComponent (App layout)
         ↓
Feature Modules
    ├── Auth Module (Login/Register)
    ├── Dashboard Module (Overview)
    ├── Projects Module (Project management)
    └── Tasks Module (Task management + Kanban)
         ↓
Shared Module
    └── Material UI Components
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL 16
- **ORM**: Hibernate/JPA
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Build Tool**: Maven
- **Health Monitoring**: Spring Boot Actuator

### Frontend
- **Framework**: Angular 17
- **UI Library**: Angular Material
- **State Management**: RxJS
- **Drag & Drop**: Angular CDK
- **HTTP Client**: Angular HttpClient
- **Routing**: Angular Router
- **Build Tool**: Angular CLI

### DevOps
- **Containerization**: Docker & Docker Compose
- **Web Server**: Nginx (for Angular)
- **Database**: PostgreSQL with persistent volumes

## 📋 Prerequisites

### Option 1: Docker (Recommended)
- Docker 20.10+
- Docker Compose 2.0+

### Option 2: Local Development
- Java 17 or higher
- Node.js 20 or higher
- PostgreSQL 16
- Maven 3.9+
- Angular CLI 17

## 🚀 Quick Start with Docker

### 1. Clone the Repository
```bash
git clone <repository-url>
cd smart-project-platform
```

### 2. Build and Run with Docker Compose
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

### 3. Access the Application
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database**: localhost:5432

### 4. Stop the Application
```bash
docker-compose down

# To remove volumes as well
docker-compose down -v
```

## 💻 Local Development Setup

### Backend Setup

1. **Configure Database**
   ```bash
   # Create PostgreSQL database
   createdb smart_project_db
   ```

2. **Update application.yml**
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/smart_project_db
       username: your_username
       password: your_password
   ```

3. **Build and Run**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

   The backend will start on http://localhost:8080

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Run Development Server**
   ```bash
   npm start
   # or
   ng serve
   ```

   The frontend will start on http://localhost:4200

3. **Build for Production**
   ```bash
   npm run build
   ```

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "user-id",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### Project Endpoints

#### Create Project
```http
POST /api/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Project",
  "description": "Project description"
}
```

#### Get All Projects (Paginated)
```http
GET /api/projects?page=0&size=10
Authorization: Bearer <token>
```

#### Update Project
```http
PUT /api/projects/{projectId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Project",
  "description": "Updated description"
}
```

#### Delete Project
```http
DELETE /api/projects/{projectId}
Authorization: Bearer <token>
```

### Task Endpoints

#### Create Task
```http
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "New Task",
  "description": "Task description",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2024-12-31",
  "projectId": "project-id"
}
```

#### Get Tasks by Project (Paginated)
```http
GET /api/tasks?projectId={projectId}&page=0&size=10
Authorization: Bearer <token>
```

#### Update Task
```http
PUT /api/tasks/{taskId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Task",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

#### Delete Task
```http
DELETE /api/tasks/{taskId}
Authorization: Bearer <token>
```

### Interactive API Documentation
Visit http://localhost:8080/swagger-ui.html for full interactive API documentation.

## 🔐 Security

### JWT Authentication
- Token-based authentication using JWT
- Tokens expire after 24 hours (configurable)
- Secure password encryption using BCrypt
- Role-based access control (RBAC)

### Security Headers
- X-Frame-Options: SAMEORIGIN
- X-Content-Type-Options: nosniff
- X-XSS-Protection: 1; mode=block

### CORS Configuration
The backend is configured to accept requests from the frontend origin. Update the CORS configuration in `WebSecurityConfig.java` for production environments.

## 🗄️ Database Schema

### Users Table
```sql
- id (UUID, PK)
- username (VARCHAR, UNIQUE)
- email (VARCHAR, UNIQUE)
- password (VARCHAR, encrypted)
- full_name (VARCHAR)
- role (VARCHAR)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Projects Table
```sql
- id (UUID, PK)
- name (VARCHAR)
- description (TEXT)
- owner_id (UUID, FK -> users)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Tasks Table
```sql
- id (UUID, PK)
- title (VARCHAR)
- description (TEXT)
- status (ENUM: TODO, IN_PROGRESS, DONE)
- priority (ENUM: LOW, MEDIUM, HIGH)
- due_date (DATE)
- project_id (UUID, FK -> projects)
- assignee_id (UUID, FK -> users)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Activity Logs Table
```sql
- id (UUID, PK)
- user_id (UUID, FK -> users)
- action (VARCHAR)
- entity_type (VARCHAR)
- entity_id (VARCHAR)
- timestamp (TIMESTAMP)
- details (TEXT)
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test

# For code coverage
npm run test:coverage
```

## 📦 Deployment

### Production Build

#### Backend
```bash
cd backend
mvn clean package -DskipTests
java -jar target/platform-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend
npm run build
# Deploy the dist/ folder to your web server
```

### Docker Deployment
```bash
# Build for production
docker-compose -f docker-compose.yml up -d --build

# View logs
docker-compose logs -f

# Scale services
docker-compose up -d --scale backend=3
```

### Environment Variables for Production
```bash
# Backend
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/smart_project_db
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_secure_password
JWT_SECRET=your_very_long_and_secure_jwt_secret_key
JWT_EXPIRATION_MS=86400000

# Frontend (update environment.prod.ts)
API_URL=https://your-api-domain.com
```

## 🔧 Configuration

### Backend Configuration (application.yml)
Key configurations in `backend/src/main/resources/application.yml`:
- Database connection settings
- JWT secret and expiration
- Flyway migration settings
- Logging levels
- Actuator endpoints

### Frontend Configuration
Environment configurations in `frontend/src/environments/`:
- `environment.ts` - Development
- `environment.prod.ts` - Production

## 📊 Monitoring & Health Checks

### Health Check Endpoints
- **Backend Health**: http://localhost:8080/actuator/health
- **Frontend Health**: http://localhost/health
- **Database**: Checked via backend actuator

### Docker Health Checks
All services include health checks:
- PostgreSQL: `pg_isready`
- Backend: Actuator health endpoint
- Frontend: Nginx status

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👥 Authors

- Your Name - Initial work

## 🙏 Acknowledgments

- Spring Boot Team
- Angular Team
- Material Design Team
- PostgreSQL Community

## 📞 Support

For support, email support@example.com or open an issue in the repository.

## 🗺️ Roadmap

See the open issues and project board for planned features and known issues.

---

**Built with ❤️ using Spring Boot and Angular**
