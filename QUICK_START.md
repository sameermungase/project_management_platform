# Quick Start Guide
## Smart Project and Collaboration Platform

Get your platform up and running in minutes!

---

## 🚀 Quick Start (Docker - Recommended)

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

### Start the Application

#### Option 1: Using the Deployment Script (Linux/Mac)
```bash
# Make the script executable
chmod +x deploy.sh

# Run the script
./deploy.sh

# Select option 2 for production mode
```

#### Option 2: Manual Docker Compose
```bash
# Start all services
docker-compose up -d --build

# View logs
docker-compose logs -f
```

### Access the Application
Once all services are running:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html

### Stop the Application
```bash
docker-compose down

# To remove data volumes as well
docker-compose down -v
```

---

## 🎯 First Steps

### 1. Register a New User
1. Open http://localhost in your browser
2. Click "Register" or navigate to the register page
3. Fill in your details:
   - Username
   - Email
   - Password
   - Full Name
4. Click "Register"

### 2. Login
1. Navigate to the login page
2. Enter your username and password
3. Click "Login"

### 3. Create Your First Project
1. Click on "Projects" in the sidebar
2. Click the "+ New Project" button
3. Enter project details:
   - Project Name
   - Description
4. Click "Save"

### 4. Create Tasks
1. Click on your project card
2. Click "Tasks" button
3. Click "+ New Task"
4. Fill in task details:
   - Title
   - Description
   - Status (TODO, IN_PROGRESS, DONE)
   - Priority (LOW, MEDIUM, HIGH)
   - Due Date
5. Click "Save"

### 5. Use the Kanban Board
1. From the Tasks page, click "Kanban Board"
2. Drag and drop tasks between columns (TODO → IN_PROGRESS → DONE)
3. Tasks automatically update when moved

---

## 🔧 Local Development Setup (Without Docker)

### Prerequisites
- Java 17+
- Node.js 20+
- PostgreSQL 16
- Maven 3.9+

### 1. Setup PostgreSQL
```bash
# Create database
createdb smart_project_db

# Or using psql
psql -U postgres
CREATE DATABASE smart_project_db;
\q
```

### 2. Configure Backend
```bash
cd backend

# Update src/main/resources/application.yml
# Change database credentials if needed

# Run the application
mvn clean install
mvn spring-boot:run
```

Backend will start on http://localhost:8080

### 3. Configure Frontend
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

Frontend will start on http://localhost:4200

---

## 📱 Using the Application

### Project Management
- **Create Projects**: Click "+ New Project"
- **Edit Projects**: Click "Edit" on any project card
- **Delete Projects**: Click "Delete" (confirmation required)
- **View Tasks**: Click "Tasks" button on a project

### Task Management
- **List View**: Traditional table view with pagination
- **Kanban View**: Drag-and-drop board for visual management
- **Create Tasks**: Use "+ New Task" button
- **Edit Tasks**: Click edit icon on any task
- **Delete Tasks**: Click delete icon (confirmation required)
- **Change Status**: Drag tasks between columns in Kanban view

### Task Priorities
- 🔴 **HIGH**: Urgent tasks
- 🟡 **MEDIUM**: Normal priority
- 🟢 **LOW**: Can wait

### Task Statuses
- **TODO**: Not started
- **IN_PROGRESS**: Currently working on
- **DONE**: Completed

---

## 🔑 API Usage

### Authentication

#### Register
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": "user-id",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### Using the API

Store the token and use it in subsequent requests:

```bash
# Set token variable
TOKEN="your-jwt-token-here"

# Create a project
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Project",
    "description": "Project description"
  }'

# Get all projects
curl -X GET "http://localhost:8080/api/projects?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Task",
    "description": "Task description",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2024-12-31",
    "projectId": "project-id-here"
  }'
```

---

## 🐛 Troubleshooting

### Docker Issues

#### Ports Already in Use
```bash
# Check what's using the port
# Windows
netstat -ano | findstr :8080
netstat -ano | findstr :5432

# Linux/Mac
lsof -i :8080
lsof -i :5432

# Kill the process or change ports in docker-compose.yml
```

#### Container Won't Start
```bash
# View logs
docker-compose logs backend
docker-compose logs postgres
docker-compose logs frontend

# Rebuild from scratch
docker-compose down -v
docker-compose up --build --force-recreate
```

#### Database Connection Issues
```bash
# Check if postgres is healthy
docker-compose ps

# Check postgres logs
docker-compose logs postgres

# Restart postgres
docker-compose restart postgres
```

### Backend Issues

#### Application Won't Start
- Check Java version: `java -version` (should be 17+)
- Check PostgreSQL is running
- Check database credentials in `application.yml`
- Check logs in console

#### Authentication Errors
- Check JWT secret is properly configured
- Token might be expired (default: 24 hours)
- Login again to get a new token

### Frontend Issues

#### Can't Connect to Backend
- Check backend is running on port 8080
- Check CORS configuration in backend
- Check browser console for errors

#### Build Errors
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear Angular cache
rm -rf .angular
ng build
```

---

## 📊 Health Checks

### Backend Health
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Database Health
```bash
# Using docker
docker-compose exec postgres pg_isready -U postgres

# Using psql
psql -h localhost -U postgres -c "SELECT 1"
```

### Frontend Health
```bash
curl http://localhost/health
```

---

## 🔄 Update & Maintenance

### Update Application
```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

### Backup Database
```bash
# Create backup
docker-compose exec postgres pg_dump -U postgres smart_project_db > backup.sql

# Restore backup
docker-compose exec -T postgres psql -U postgres smart_project_db < backup.sql
```

### View Application Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100 backend
```

---

## 📚 Next Steps

1. **Explore the API**: Visit http://localhost:8080/swagger-ui.html
2. **Read the Full Documentation**: Check README.md
3. **Feature Recommendations**: See FEATURE_RECOMMENDATIONS.md for enhancement ideas
4. **Customize**: Modify configurations for your needs
5. **Deploy to Production**: Follow production deployment guide in README.md

---

## 🆘 Getting Help

If you encounter issues:
1. Check the troubleshooting section above
2. Review application logs
3. Check Docker logs
4. Consult the full README.md
5. Open an issue on the repository

---

## ⚙️ Configuration

### Environment Variables
Key environment variables (set in docker-compose.yml or .env file):

```bash
# Database
POSTGRES_DB=smart_project_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_MS=86400000

# Ports
BACKEND_PORT=8080
FRONTEND_PORT=80
POSTGRES_PORT=5432
```

### Change Ports
Edit `docker-compose.yml`:
```yaml
services:
  frontend:
    ports:
      - "3000:80"  # Change 80 to your desired port
  
  backend:
    ports:
      - "9000:8080"  # Change 8080 to your desired port
```

---

## 🎉 Success!

You're all set! Start managing your projects efficiently with the Smart Project Platform.

**Happy Project Managing! 🚀**
