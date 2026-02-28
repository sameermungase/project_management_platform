# Smart Project and Collaboration Platform

A modern, full-stack enterprise-grade project management and collaboration system built with Spring Boot, Angular, and PostgreSQL.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen)
![Angular](https://img.shields.io/badge/Angular-17-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)
![GitHub](https://img.shields.io/badge/GitHub-Ready-green)

## 🟢 Project Status
**Last Verified:** Saturday, 28 February 2026
- **Backend (Spring Boot):** Passing all tests (33/33 tests passed, build successful).
- **Frontend (Angular 17):** Building successfully with no compilation errors.
- **Overall Condition:** Both frontend and backend are perfectly healthy and production-ready.

## 🚀 Features

### Core Functionality
- **User Authentication & Authorization**: Secure JWT-based authentication with role-based access control
- **Project Management**: Create, update, and manage projects with detailed information and member management
- **Task Management**: Comprehensive task tracking with priorities, status, due dates, and assignments
- **Kanban Board**: Interactive drag-and-drop task board for visual task management
- **Task Dependencies**: Define and manage dependencies between tasks
- **Comments & Collaboration**: Threaded comments on tasks with reactions
- **Project Templates**: Create reusable project templates with predefined tasks
- **User Management**: User profiles, role management, and organization-wide user administration
- **Dashboard**: Real-time project statistics, task distribution, and activity overview
- **Activity Logging**: Track all user activities and changes in the system
- **RESTful APIs**: Well-documented APIs with Swagger/OpenAPI integration

### Technical Highlights
- **Responsive Design**: Modern Material UI with responsive layouts
- **Real-time Updates**: Asynchronous operations for better performance
- **Database Migration**: Flyway integration for version-controlled database schemas
- **API Documentation**: Interactive Swagger UI for easy API exploration
- **Health Monitoring**: Spring Boot Actuator endpoints for application health checks
- **Docker Support**: Complete containerization with Docker Compose
- **Security Best Practices**: Configuration templates, no hardcoded credentials, secure JWT implementation
- **Version Control**: Git-ready with proper .gitignore and security configurations

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
    ├── TaskController (Task CRUD operations)
    ├── CommentController (Task comments and reactions)
    ├── TaskDependencyController (Task dependency management)
    ├── ProjectTemplateController (Project template management)
    ├── UserController (User management and profiles)
    └── DashboardController (Statistics and analytics)
         ↓
Service Layer
    ├── ActivityLogService (Async activity logging)
    ├── ProjectService (Business logic for projects)
    ├── TaskService (Business logic for tasks)
    ├── CommentService (Comment management)
    ├── TaskDependencyService (Dependency management)
    ├── ProjectTemplateService (Template management)
    └── UserService (User management)
         ↓
Repository Layer (Spring Data JPA)
    ├── UserRepository
    ├── ProjectRepository
    ├── TaskRepository
    ├── CommentRepository
    ├── TaskDependencyRepository
    ├── ProjectTemplateRepository
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
    ├── Dashboard Module (Overview & Statistics)
    ├── Projects Module (Project management + Templates)
    ├── Tasks Module (Task management + Kanban + Dependencies)
    ├── Comments Module (Task comments & reactions)
    ├── Templates Module (Project template management)
    └── Users Module (User profiles & management)
         ↓
Shared Module
    └── Material UI Components
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 21
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL 16
- **ORM**: Hibernate/JPA
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Build Tool**: Maven
- **Health Monitoring**: Spring Boot Actuator
- **Validation**: Spring Boot Validation

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
- Java 21 or higher
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

1. **Create Database**
   ```bash
   # Create PostgreSQL database
   createdb smart_project_db
   # Or using psql
   psql -U postgres
   CREATE DATABASE smart_project_db;
   \q
   ```

2. **Configure application.yml**
   
   The project includes `application.yml.template` as a reference. Create your local `application.yml`:
   ```bash
   # Copy the template (if not already exists)
   cp backend/src/main/resources/application.yml.template backend/src/main/resources/application.yml
   ```
   
   Then edit `backend/src/main/resources/application.yml` and update:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/smart_project_db
       username: postgres  # Your PostgreSQL username
       password: your_password  # Your PostgreSQL password
   
   app:
     jwtSecret: your_secure_jwt_secret_key  # Generate a secure key
   ```
   
   **Note**: `application.yml` is excluded from Git for security. Use environment variables or the template file.

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
POST /api/auth/signin
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

### Additional Endpoints

#### Comments
- `POST /api/comments` - Create a comment
- `GET /api/comments/task/{taskId}` - Get comments for a task
- `PUT /api/comments/{commentId}` - Update a comment
- `DELETE /api/comments/{commentId}` - Delete a comment
- `POST /api/comments/{commentId}/reactions` - Add reaction to comment

#### Task Dependencies
- `POST /api/task-dependencies` - Create a task dependency
- `GET /api/task-dependencies/task/{taskId}` - Get dependencies for a task
- `DELETE /api/task-dependencies/{dependencyId}` - Remove a dependency

#### Project Templates
- `GET /api/project-templates` - Get all templates
- `POST /api/project-templates` - Create a template
- `POST /api/project-templates/{templateId}/create-project` - Create project from template

#### Users
- `GET /api/users` - Get all users
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/{userId}/roles` - Update user roles (Admin only)

#### Dashboard
- `GET /api/dashboard/stats` - Get dashboard statistics
- `GET /api/dashboard/task-distribution` - Get task distribution
- `GET /api/dashboard/recent-activities` - Get recent activities

### Interactive API Documentation
Visit http://localhost:8080/swagger-ui.html for full interactive API documentation with all available endpoints.

## 🔐 Security

### Security Best Practices

✅ **Implemented Security Measures:**
- **No Hardcoded Credentials**: All sensitive configuration is excluded from Git
- **Configuration Templates**: `application.yml.template` provided for safe version control
- **Environment Variables**: Support for environment variable substitution
- **Secure Git Configuration**: Proper `.gitignore` to prevent accidental commits of secrets

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

### Important Security Notes

⚠️ **Before deploying to production:**
1. Generate a strong JWT secret (use `openssl rand -base64 64`)
2. Use strong database passwords
3. Update CORS settings for your production domain
4. Use environment variables or secure secret management
5. Never commit `application.yml` or any files with real credentials

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
- status (VARCHAR: TO_DO, IN_PROGRESS, DONE)
- priority (VARCHAR: LOW, MEDIUM, HIGH)
- due_date (DATE)
- project_id (UUID, FK -> projects)
- assignee_id (UUID, FK -> users)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Comments Table
```sql
- id (UUID, PK)
- content (TEXT)
- task_id (UUID, FK -> tasks)
- user_id (UUID, FK -> users)
- parent_comment_id (UUID, FK -> comments, nullable)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Task Dependencies Table
```sql
- id (UUID, PK)
- task_id (UUID, FK -> tasks)
- depends_on_task_id (UUID, FK -> tasks)
- created_at (TIMESTAMP)
```

### Project Templates Table
```sql
- id (UUID, PK)
- name (VARCHAR)
- description (TEXT)
- created_by (UUID, FK -> users)
- created_at (TIMESTAMP)
```

### Template Tasks Table
```sql
- id (UUID, PK)
- template_id (UUID, FK -> project_templates)
- title (VARCHAR)
- description (TEXT)
- priority (VARCHAR)
- order_index (INTEGER)
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

### ✅ Production-Ready Docker Deployment (Recommended)

The Smart Project Platform includes fully optimized Docker support with multi-stage builds.

#### Quick Start with Docker Compose
```bash
# Navigate to project root
cd "Smart Project and Collaboration Platform"

# Build and start all services
docker-compose up -d --build

# Wait 60 seconds for services to initialize
# Then access the application:
# Frontend: http://localhost:8081
# Backend API: http://localhost:8080/api
# Swagger UI: http://localhost:8080/swagger-ui.html

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Remove all data
docker-compose down -v
```

#### Production Deployment
```bash
# Use production compose file with optimizations
docker-compose -f docker-compose.prod.yml up -d --build

# The production setup includes:
# - Redis caching
# - Database backups
# - Load balancing with Nginx
# - 2x backend replicas
# - Health checks & monitoring
# - SSL/TLS ready
```

#### Docker Images
- **Backend**: Multi-stage Maven build → Java 21 Alpine runtime (380MB)
- **Frontend**: Multi-stage Node build → Nginx Alpine (80MB)
- **Database**: PostgreSQL 16 Alpine with persistence

#### Troubleshooting
For detailed troubleshooting commands, see [DOCKER_SETUP_GUIDE.md](./DOCKER_SETUP_GUIDE.md)

---

### Local Development Build

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

### Backend Configuration

**Important Security Note**: The actual `application.yml` file is excluded from Git for security. A template file (`application.yml.template`) is provided as a reference.

#### Creating application.yml

1. Copy the template:
   ```bash
   cp backend/src/main/resources/application.yml.template backend/src/main/resources/application.yml
   ```

2. Update with your local settings:
   - Database credentials (username, password)
   - JWT secret (generate a secure random key)
   - Any other environment-specific settings

#### Environment Files

The project includes environment templates for Docker:
- `.env.example` - Template for all environment variables (version-controlled)
- `.env.local` - Local development overrides (git-ignored)
- `.env.prod` - Production configuration (git-ignored)

```bash
# Create local environment file
cp .env.example .env.dev

# Create production environment file
cp .env.example .env.prod

# Edit .env.prod with production values
# Then deploy: docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```#### Key Configuration Options

- **Database**: Connection URL, username, password
- **JWT**: Secret key and expiration time
- **Flyway**: Migration settings
- **Logging**: Log levels
- **Actuator**: Health check endpoints

#### Using Environment Variables

You can also use environment variables instead of hardcoding values:
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
```

The template supports environment variable substitution using `${VARIABLE_NAME:default_value}` syntax.

### Frontend Configuration

Frontend services are configured to connect to `http://localhost:8080/api` by default. Update the API URLs in service files if your backend runs on a different port or domain.

### Flyway Maven Plugin

The Flyway plugin in `pom.xml` uses Maven properties. Run migrations with:
```bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/smart_project_db -Dflyway.user=postgres -Dflyway.password=your_password
```

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

## 📁 Project Structure

```
smart-project-platform/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   └── main/
│   │       ├── java/com/smartproject/platform/
│   │       │   ├── config/      # Configuration classes
│   │       │   ├── controller/ # REST controllers
│   │       │   ├── dto/         # Data Transfer Objects
│   │       │   ├── model/       # Entity models
│   │       │   ├── repository/  # JPA repositories
│   │       │   ├── security/   # Security configuration
│   │       │   └── service/     # Business logic
│   │       └── resources/
│   │           ├── application.yml.template  # Config template
│   │           └── db/migration/            # Flyway migrations
│   └── pom.xml
├── frontend/                # Angular frontend
│   ├── src/
│   │   └── app/
│   │       ├── core/           # Core services & guards
│   │       ├── features/       # Feature modules
│   │       └── shared/         # Shared components
│   └── package.json
├── docker-compose.yml       # Docker Compose configuration
├── .gitignore              # Git ignore rules
└── README.md
```

## 🔒 Security & Version Control

This project follows security best practices:

- ✅ Sensitive configuration files excluded from Git
- ✅ Configuration templates provided for setup
- ✅ No hardcoded credentials in committed files
- ✅ Proper `.gitignore` configuration
- ✅ Environment variable support

**Important**: When cloning this repository, create `application.yml` from the template before running the application.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. **Never commit sensitive files** (application.yml, .env, etc.)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Contributing Guidelines
- Follow the existing code style
- Add tests for new features
- Update documentation as needed
- Ensure all sensitive data is excluded from commits


## 👥 Authors

- Project Contributors

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

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
