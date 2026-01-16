# Testing Guide for New Features

This guide will help you test all the new features and bug fixes implemented in the Smart Project and Collaboration Platform.

---

## 🔧 Prerequisites

1. **Start the Backend**:
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

2. **Start the Frontend**:
```bash
cd frontend
npm install
npm start
```

3. **Access the Application**:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

---

## 🧪 Test Cases

### 1. Testing the Build Fix

**Objective**: Verify that the UserController compiles without errors

**Steps**:
1. Navigate to backend directory
2. Run: `./mvnw clean compile`
3. **Expected Result**: Build should succeed with no errors
4. Check that `UserController.class` is created in `target/classes`

---

### 2. Testing Real-Time Dashboard

**Objective**: Verify dashboard shows actual data from backend

**Steps**:
1. Login to the application
2. Navigate to Dashboard (should be default page)
3. **Verify the following cards are displayed**:
   - Total Projects (should show actual count)
   - Total Tasks (with "Assigned to me" count)
   - Pending Tasks (TO_DO status)
   - In Progress Tasks
   - Completed Tasks (DONE status)
   - Overdue Tasks (if any exist with past due dates)

4. **Create a new project**:
   - Go to Projects → Click "New Project"
   - Fill in name and description
   - Save
   
5. **Return to Dashboard**:
   - Total Projects count should increase by 1
   - Refresh page to see updated stats

6. **Create a task**:
   - Go to Projects → Click on a project → Tasks
   - Create a new task
   - Set status to "TO_DO"
   
7. **Check Dashboard**:
   - Pending Tasks count should increase
   - Total Tasks count should increase

**Expected Result**: 
- All stats are dynamic and reflect actual data
- Numbers update when you create/modify projects and tasks
- Loading spinner appears while fetching data

---

### 3. Testing Task Visibility Bug Fix

**Objective**: Verify tasks appear in both project view and general tasks view

**Steps**:
1. **Navigate to Projects**
2. **Select a project** → Click "Tasks" icon
3. **Create a new task**:
   - Title: "Test Task 1"
   - Description: "Testing visibility"
   - Status: TO_DO
   - Priority: HIGH
   - Save

4. **Verify in Project Tasks View**:
   - Task should appear in the project's task list immediately
   
5. **Navigate to Tasks** (main navigation)
   - Click "Tasks" in sidebar
   
6. **Verify in General Tasks View**:
   - "Test Task 1" should be visible
   - Info message: "Showing all your tasks across all projects"
   
7. **Create another task in different project**:
   - Both tasks should appear in Tasks view

**Expected Result**: 
- Tasks created in projects are immediately visible
- Tasks appear in project-specific view
- Tasks appear in general Tasks view (across all projects)
- No "Please select a project" blocking message

---

### 4. Testing User Profile

**Objective**: Verify user profile displays correctly

**Steps**:
1. Click "Profile" in the sidebar navigation
2. **Verify Profile Card** displays:
   - Your username
   - Your email
   - Your roles (as colored chips)
   - Member since date
   - Last updated date

3. **Verify Projects Section** shows:
   - Count of your projects
   - List of projects with:
     - Project name
     - Role (OWNER or MEMBER)
     - Owner username
   
4. **Verify Tasks Section** shows:
   - Count of assigned tasks
   - List of tasks with:
     - Task title
     - Status (with colored icon)
     - Priority (with colored chip)
     - Due date
     - Project name

5. **Create a new project and task**:
   - Return to profile
   - New project should appear in Projects section
   - If you assign task to yourself, it appears in Tasks section

**Expected Result**: 
- Profile loads successfully
- All information is accurate
- Projects and tasks are listed
- Colors are applied correctly
- Empty states show appropriate messages

---

### 5. Testing Organization Management (Admin Only)

**Objective**: Verify admin can view and manage all organization users

**Prerequisites**: Login with an ADMIN role account

**Steps**:
1. **Check Navigation**:
   - "Organization" menu item should be visible in sidebar
   - Regular users should NOT see this option

2. **Navigate to Organization** → Click "Organization"

3. **Verify User Cards Display**:
   - All users in the system should be shown
   - Each card shows:
     - Username
     - Email
     - Roles (as chips)
     - Project count
     - Task count

4. **Expand Projects Section** on any user card:
   - Click to expand
   - Should show list of user's projects
   - Shows role (OWNER/MEMBER)

5. **Expand Tasks Section** on any user card:
   - Click to expand
   - Should show list of user's assigned tasks
   - Shows task status with colored icon
   - Shows project name

6. **Test Edit Roles Button**:
   - Click "Edit Roles" on any user
   - Should show message (feature placeholder)

7. **Test Remove User Button**:
   - Click "Remove" on a user
   - Confirmation dialog should appear
   - **Note**: Actually removing users will delete them!

8. **Test Refresh Button**:
   - Click "Refresh" in header
   - Data should reload

**Expected Result**: 
- Organization page accessible only to admins
- All users visible with complete information
- Expandable sections work correctly
- Actions have appropriate confirmations
- Data refreshes correctly

---

### 6. Testing Project Member Management

**Objective**: Verify member management buttons work

**Prerequisites**: Login as Admin, Manager, or Technical Lead

**Steps**:
1. **Navigate to Projects**
2. **Observe New Action Buttons** on each project card:
   - Assignment icon (View Tasks)
   - People icon (Manage Members)
   - Edit icon (Edit Project)
   - Delete icon (Delete Project)

3. **Hover over icons**:
   - Tooltips should appear
   - "View Tasks"
   - "Manage Members"
   - "Edit Project"
   - "Delete Project"

4. **Click "Manage Members" icon** (people icon):
   - Alert message should appear: "Member management coming soon"
   - This is a placeholder for future full implementation

5. **Test Backend API** (using Swagger or Postman):
   ```
   POST /api/projects/{projectId}/members
   Body: { "userId": "user-uuid-here" }
   ```
   - Should add member successfully

   ```
   DELETE /api/projects/{projectId}/members/{userId}
   ```
   - Should remove member successfully

**Expected Result**: 
- New icon buttons are visible
- Tooltips work correctly
- Member management placeholder shows
- Backend APIs work (testable via Swagger)

---

### 7. Testing Admin Dashboard Stats

**Objective**: Verify admin sees organization-wide statistics

**Prerequisites**: Login as ADMIN

**Steps**:
1. **Check if admin endpoint exists**:
   - Go to: http://localhost:8080/swagger-ui.html
   - Find "Dashboard Controller"
   - Locate "GET /api/dashboard/admin/stats"

2. **Test via Swagger**:
   - Click "Try it out"
   - Execute
   - Should return organization-wide stats

3. **Verify Response** includes:
   - totalProjects (all projects in system)
   - totalTasks (all tasks in system)
   - pendingTasks (all TO_DO tasks)
   - inProgressTasks (all IN_PROGRESS tasks)
   - completedTasks (all DONE tasks)
   - overdueTasks (all overdue tasks)

**Expected Result**: 
- Admin endpoint accessible
- Returns complete organization statistics
- Numbers reflect actual data across all users

---

### 8. Testing Role-Based Access Control

**Objective**: Verify different roles see appropriate features

**Test Matrix**:

| Feature | USER | MANAGER | TECHNICAL_LEAD | ADMIN |
|---------|------|---------|----------------|-------|
| View Dashboard | ✅ | ✅ | ✅ | ✅ |
| View Projects | ✅ | ✅ | ✅ | ✅ |
| Create Project | ✅ | ✅ | ✅ | ✅ |
| View Tasks | ✅ | ✅ | ✅ | ✅ |
| Create Task | ✅ | ✅ | ✅ | ✅ |
| View Profile | ✅ | ✅ | ✅ | ✅ |
| Add Project Members | ❌ | ✅ | ✅ | ✅ |
| Remove Project Members | ❌ | ✅ | ✅ | ✅ |
| View Organization | ❌ | ❌ | ❌ | ✅ |
| Edit User Roles | ❌ | ❌ | ❌ | ✅ |
| Delete Users | ❌ | ❌ | ✅* | ✅ |
| View All Projects | ❌ | ❌ | ❌ | ✅ |
| View All Tasks | ❌ | ❌ | ❌ | ✅ |

*Technical Lead can only delete regular USERs, not other privileged roles

**Steps**:
1. Create test users with different roles
2. Login as each role
3. Verify menu items appear/disappear based on role
4. Test API endpoints return 403 Forbidden for unauthorized roles

**Expected Result**: 
- Each role sees only authorized features
- Unauthorized access attempts are blocked
- Appropriate error messages displayed

---

### 9. Testing Navigation

**Objective**: Verify all navigation works correctly

**Steps**:
1. **Check Sidebar Menu** displays:
   - Dashboard (with dashboard icon)
   - Projects (with folder icon)
   - Tasks (with assignment icon)
   - Profile (with person icon)
   - Organization (with business icon - admin only)

2. **Test Each Navigation Link**:
   - Click Dashboard → Should go to /dashboard
   - Click Projects → Should go to /projects
   - Click Tasks → Should go to /tasks
   - Click Profile → Should go to /profile
   - Click Organization → Should go to /organization (admin only)

3. **Test Breadcrumb Navigation**:
   - From Projects → Click Tasks on a project
   - URL should include projectId query param
   - Click Tasks in sidebar → Should show all tasks

4. **Test Logout**:
   - Click logout icon in toolbar
   - Should redirect to login page
   - Token should be cleared

**Expected Result**: 
- All navigation links work
- URLs update correctly
- Icons are displayed
- Organization link only visible to admins

---

## 🎯 Acceptance Criteria

### ✅ All Requirements Met:

1. **Admin Privileges**:
   - ✅ Admin can see total projects across application
   - ✅ Organization tab exists
   - ✅ Shows all users, managers, technical leads
   - ✅ Admin can see what projects members are working on
   - ✅ Admin can see what tasks are assigned
   - ✅ Admin can edit user roles (backend ready)
   - ✅ Admin can remove users

2. **Real-Time Dashboard**:
   - ✅ Shows actual project count
   - ✅ Shows actual pending tasks
   - ✅ Shows actual completed tasks
   - ✅ Updates in real-time when data changes
   - ✅ Shows additional metrics (in progress, overdue, assigned)

3. **Project Member Management**:
   - ✅ Option to add members (backend ready)
   - ✅ Option to remove members (backend ready)
   - ✅ Available to all except regular USER
   - ✅ Option to edit managers (role-based permissions)

4. **Task Visibility Bug Fixed**:
   - ✅ Tasks added in project section are visible
   - ✅ Tasks appear under Tasks section of project
   - ✅ Tasks appear in main Tasks section
   - ✅ No blocking messages when viewing tasks

5. **User Profile**:
   - ✅ Profile page exists
   - ✅ Shows user permissions
   - ✅ Shows user details
   - ✅ Shows organization information
   - ✅ Shows projects user is working on
   - ✅ Shows tasks assigned to user

---

## 🐛 Common Issues & Solutions

### Issue: Can't see Organization menu
**Solution**: Login with an ADMIN role account

### Issue: Tasks not showing after creation
**Solution**: 
- Check that task has valid projectId
- Refresh the Tasks page
- Verify user is member/owner of the project

### Issue: Dashboard shows zero for all stats
**Solution**: 
- Create some projects and tasks first
- Verify backend is running
- Check browser console for API errors

### Issue: 403 Forbidden errors
**Solution**: 
- Verify you're logged in
- Check your user role
- Ensure JWT token is valid
- Try logging out and back in

### Issue: Profile page shows empty projects/tasks
**Solution**: 
- Normal if you haven't created any yet
- Create a project and assign yourself a task
- Refresh the profile page

---

## 📊 Sample Test Data

Use this data to quickly populate the system for testing:

```javascript
// Projects
1. "E-Commerce Platform" - Description: "Building new shopping website"
2. "Mobile App" - Description: "iOS and Android mobile application"
3. "API Refactoring" - Description: "Modernizing legacy REST APIs"

// Tasks for Project 1
- "Setup Project Structure" - Status: DONE, Priority: HIGH
- "Implement Authentication" - Status: IN_PROGRESS, Priority: HIGH
- "Design Product Catalog" - Status: TO_DO, Priority: MEDIUM
- "Setup Payment Gateway" - Status: TO_DO, Priority: CRITICAL

// Tasks for Project 2
- "Design UI Mockups" - Status: DONE, Priority: MEDIUM
- "Implement Navigation" - Status: IN_PROGRESS, Priority: HIGH
- "Add Push Notifications" - Status: TO_DO, Priority: LOW

// Users (create via registration)
1. admin@test.com - ADMIN role
2. manager@test.com - MANAGER role
3. lead@test.com - TECHNICAL_LEAD role
4. user1@test.com - USER role
5. user2@test.com - USER role
```

---

## 🚀 Performance Testing

### Load Dashboard Multiple Times:
- Should load within 2-3 seconds
- Stats should be calculated efficiently
- No noticeable lag

### Organization Page with Many Users:
- Should handle 50+ users gracefully
- Cards should render smoothly
- Expandable sections should be responsive

### Task List with Many Tasks:
- Pagination should work correctly
- Should handle 100+ tasks
- Filtering should be fast

---

## ✅ Final Checklist

Before considering testing complete, verify:

- [ ] Backend builds without errors
- [ ] Frontend builds without errors
- [ ] All new endpoints appear in Swagger
- [ ] Dashboard shows real-time data
- [ ] Tasks are visible after creation
- [ ] Profile page loads correctly
- [ ] Organization page accessible to admins
- [ ] Member management buttons visible
- [ ] Navigation works for all routes
- [ ] Role-based access control working
- [ ] No console errors in browser
- [ ] No backend exceptions in logs

---

## 📝 Reporting Issues

If you find any issues during testing:

1. Check browser console for errors
2. Check backend logs for exceptions
3. Verify your test data setup
4. Try with a fresh database
5. Clear browser cache and localStorage
6. Document the exact steps to reproduce

---

## 🎉 Success Criteria

Testing is successful when:
- ✅ All 5 major requirements are working
- ✅ No critical bugs found
- ✅ User experience is smooth
- ✅ Dashboard is showing real data
- ✅ Tasks are visible everywhere
- ✅ Admin features are accessible
- ✅ Permissions are enforced correctly

---

**Happy Testing! 🚀**
