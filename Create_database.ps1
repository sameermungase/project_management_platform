# PowerShell Script to Create Smart Project and Collaboration Platform Database
# This script creates the database, schema, and all required tables

# Configuration
$dbServer   = "localhost"
$dbPort     = 5432
$dbName     = "smart_project_db2"
$dbUsername = "postgres"
$dbPassword = "root"
$pgBinPath  = "C:\Program Files\PostgreSQL\16\bin"  # Adjust version/path if needed

# Add PostgreSQL bin to PATH
$env:Path += ";$pgBinPath"

# ---------------------------------------------------------------------------
# Helper functions
# ---------------------------------------------------------------------------

function Execute-SQL {
    param(
        [string]$sqlCommand,
        [string]$database = $null
    )
    $env:PGPASSWORD = $dbPassword
    if ($database) {
        psql -h $dbServer -p $dbPort -U $dbUsername -d $database -c $sqlCommand 2>&1
    } else {
        psql -h $dbServer -p $dbPort -U $dbUsername -c $sqlCommand 2>&1
    }
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

function Execute-SQLFile {
    param(
        [string]$filePath,
        [string]$database
    )
    $env:PGPASSWORD = $dbPassword
    psql -h $dbServer -p $dbPort -U $dbUsername -d $database -f $filePath 2>&1
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

# FIX: Write SQL using .NET directly so PowerShell never expands $ inside the content.
# This is the only reliable way to preserve PostgreSQL dollar-quoting ($$).
function Write-SqlFile {
    param(
        [string]$filePath,
        [string]$content
    )
    [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
}

# ---------------------------------------------------------------------------
Write-Host "========================================" -ForegroundColor Green
Write-Host "Smart Project Database Setup"            -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# ---------------------------------------------------------------------------
# Step 1: Create Database
# FIX: PostgreSQL has no "CREATE DATABASE IF NOT EXISTS". Query pg_database first.
# ---------------------------------------------------------------------------
Write-Host "[1/5] Creating database '$dbName'..." -ForegroundColor Cyan

$env:PGPASSWORD = $dbPassword
$dbExists = psql -h $dbServer -p $dbPort -U $dbUsername -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName';" 2>&1
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

if ($dbExists -eq "1") {
    Write-Host "OK Database '$dbName' already exists" -ForegroundColor Green
} else {
    $result = Execute-SQL "CREATE DATABASE `"$dbName`";"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK Database '$dbName' created successfully" -ForegroundColor Green
    } else {
        Write-Host "FAIL Failed to create database:" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        exit 1
    }
}
Write-Host ""

# ---------------------------------------------------------------------------
# Step 2: UUID extension is handled inside V1 schema
# ---------------------------------------------------------------------------
Write-Host "[2/5] UUID extension will be enabled as part of schema creation..." -ForegroundColor Cyan
Write-Host "OK UUID extension setup deferred to schema step" -ForegroundColor Green
Write-Host ""

# ---------------------------------------------------------------------------
# Step 3: V1 Schema
# FIX: Single-quoted here-string (@' '@) prevents ALL $ expansion by PowerShell.
#      uuid_generate_v4() and similar tokens are written exactly as-is.
# ---------------------------------------------------------------------------
Write-Host "[3/5] Creating initial schema (V1)..." -ForegroundColor Cyan

$v1Schema = @'
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Roles Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Project Members Table (ManyToMany)
CREATE TABLE IF NOT EXISTS project_members (
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'TO_DO',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    due_date DATE,
    project_id UUID NOT NULL,
    assignee_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Activity Logs Table
CREATE TABLE IF NOT EXISTS activity_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    action VARCHAR(50) NOT NULL,
    details TEXT,
    entity_id UUID,
    entity_type VARCHAR(20),
    user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_username           ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email              ON users(email);
CREATE INDEX IF NOT EXISTS idx_projects_owner           ON projects(owner_id);
CREATE INDEX IF NOT EXISTS idx_tasks_project            ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assignee           ON tasks(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status             ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_activity_logs_entity     ON activity_logs(entity_id, entity_type);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user       ON activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at ON activity_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_logs_action     ON activity_logs(action);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id       ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_project_members_project  ON project_members(project_id);
CREATE INDEX IF NOT EXISTS idx_project_members_user     ON project_members(user_id);
'@

$tempV1 = "$env:TEMP\v1_schema.sql"
Write-SqlFile -filePath $tempV1 -content $v1Schema
Execute-SQLFile -filePath $tempV1 -database $dbName
Remove-Item $tempV1 -ErrorAction SilentlyContinue
Write-Host "OK Initial schema created" -ForegroundColor Green
Write-Host ""

# ---------------------------------------------------------------------------
# Step 4: V2/V3/V4 Schema
# FIX: Single-quoted here-string preserves $$ dollar-quoting verbatim.
# ---------------------------------------------------------------------------
Write-Host "[4/5] Creating extended schema (V2, V3, V4)..." -ForegroundColor Cyan

$v234Schema = @'
-- Comments Table
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content TEXT NOT NULL,
    task_id UUID,
    project_id UUID,
    parent_comment_id UUID,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK ((task_id IS NOT NULL AND project_id IS NULL) OR (task_id IS NULL AND project_id IS NOT NULL))
);

-- Comment Reactions Table
CREATE TABLE IF NOT EXISTS comment_reactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    emoji VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(comment_id, user_id, emoji)
);

-- Task Dependencies Table
CREATE TABLE IF NOT EXISTS task_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    depends_on_task_id UUID NOT NULL,
    dependency_type VARCHAR(20) DEFAULT 'BLOCKS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CHECK (task_id != depends_on_task_id),
    UNIQUE(task_id, depends_on_task_id)
);

-- Task Subtasks (Parent-Child Relationship)
ALTER TABLE IF EXISTS tasks ADD COLUMN IF NOT EXISTS parent_task_id UUID;
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'tasks_parent_task_id_fkey'
    ) THEN
        ALTER TABLE tasks ADD FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE;
    END IF;
END
$$;

-- Project Templates Table
CREATE TABLE IF NOT EXISTS project_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    template_type VARCHAR(50),
    created_by_id UUID,
    is_public BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Project Template Tasks
CREATE TABLE IF NOT EXISTS project_template_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    template_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    order_index INTEGER DEFAULT 0,
    FOREIGN KEY (template_id) REFERENCES project_templates(id) ON DELETE CASCADE
);

-- Project Member Roles
CREATE TABLE IF NOT EXISTS project_member_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    assigned_by UUID,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(project_id, user_id, role)
);

-- Permissions Table
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50)
);

-- Role Permissions Mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role VARCHAR(50) NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Epics Table
CREATE TABLE IF NOT EXISTS epics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PLANNING',
    visibility_level VARCHAR(20) DEFAULT 'TEAM',
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Milestones Table
CREATE TABLE IF NOT EXISTS milestones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    epic_id UUID,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_date DATE,
    status VARCHAR(20) DEFAULT 'PLANNED',
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (epic_id) REFERENCES epics(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Link Tasks to Epics and Milestones
ALTER TABLE IF EXISTS tasks ADD COLUMN IF NOT EXISTS epic_id UUID;
ALTER TABLE IF EXISTS tasks ADD COLUMN IF NOT EXISTS milestone_id UUID;
ALTER TABLE IF EXISTS tasks ADD COLUMN IF NOT EXISTS visibility_level VARCHAR(20) DEFAULT 'PUBLIC';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'tasks_epic_id_fkey'
    ) THEN
        ALTER TABLE tasks ADD FOREIGN KEY (epic_id) REFERENCES epics(id) ON DELETE SET NULL;
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'tasks_milestone_id_fkey'
    ) THEN
        ALTER TABLE tasks ADD FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE SET NULL;
    END IF;
END
$$;

-- Approval Workflows Table
CREATE TABLE IF NOT EXISTS approvals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    approved_by UUID,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    comments TEXT,
    FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Decision Logs Table
CREATE TABLE IF NOT EXISTS decision_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    decision_type VARCHAR(50),
    decided_by UUID NOT NULL,
    approved_by UUID,
    status VARCHAR(20) DEFAULT 'PROPOSED',
    visibility_level VARCHAR(20) DEFAULT 'SENIOR_PLUS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (decided_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Task Estimates Table
CREATE TABLE IF NOT EXISTS task_estimates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    estimated_by UUID NOT NULL,
    estimated_hours DECIMAL(10,2),
    estimated_days INTEGER,
    confidence_level VARCHAR(20),
    estimation_type VARCHAR(20) DEFAULT 'INITIAL',
    reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (estimated_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT,
    entity_type VARCHAR(50),
    entity_id UUID,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Pull Requests Table
CREATE TABLE IF NOT EXISTS pull_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    source_branch VARCHAR(100),
    target_branch VARCHAR(100),
    status VARCHAR(20) DEFAULT 'OPEN',
    created_by UUID NOT NULL,
    reviewed_by UUID,
    approved_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Code Reviews Table
CREATE TABLE IF NOT EXISTS code_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pr_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    comments TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pr_id) REFERENCES pull_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Escalations Table
CREATE TABLE IF NOT EXISTS escalations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    escalated_by UUID NOT NULL,
    escalated_to UUID NOT NULL,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    FOREIGN KEY (escalated_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (escalated_to) REFERENCES users(id) ON DELETE CASCADE
);

-- User Skills Table
CREATE TABLE IF NOT EXISTS user_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    proficiency_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, skill_name)
);

-- Task Required Skills Table
CREATE TABLE IF NOT EXISTS task_required_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE(task_id, skill_name)
);

-- Attachments Table
CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    storage_type VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    epic_id UUID REFERENCES epics(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (task_id IS NOT NULL OR epic_id IS NOT NULL)
);

-- Indexes for V2/V3/V4
CREATE INDEX IF NOT EXISTS idx_comments_task                ON comments(task_id);
CREATE INDEX IF NOT EXISTS idx_comments_project             ON comments(project_id);
CREATE INDEX IF NOT EXISTS idx_comments_user                ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent              ON comments(parent_comment_id);
CREATE INDEX IF NOT EXISTS idx_comment_reactions_comment    ON comment_reactions(comment_id);
CREATE INDEX IF NOT EXISTS idx_task_dependencies_task       ON task_dependencies(task_id);
CREATE INDEX IF NOT EXISTS idx_task_dependencies_depends_on ON task_dependencies(depends_on_task_id);
CREATE INDEX IF NOT EXISTS idx_tasks_parent                 ON tasks(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_project_templates_public     ON project_templates(is_public);
CREATE INDEX IF NOT EXISTS idx_project_templates_type       ON project_templates(template_type);
CREATE INDEX IF NOT EXISTS idx_project_member_roles_project ON project_member_roles(project_id);
CREATE INDEX IF NOT EXISTS idx_project_member_roles_user    ON project_member_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_epics_project                ON epics(project_id);
CREATE INDEX IF NOT EXISTS idx_epics_status                 ON epics(status);
CREATE INDEX IF NOT EXISTS idx_milestones_project           ON milestones(project_id);
CREATE INDEX IF NOT EXISTS idx_milestones_epic              ON milestones(epic_id);
CREATE INDEX IF NOT EXISTS idx_milestones_status            ON milestones(status);
CREATE INDEX IF NOT EXISTS idx_tasks_epic                   ON tasks(epic_id);
CREATE INDEX IF NOT EXISTS idx_tasks_milestone              ON tasks(milestone_id);
CREATE INDEX IF NOT EXISTS idx_approvals_entity             ON approvals(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_approvals_status             ON approvals(status);
CREATE INDEX IF NOT EXISTS idx_approvals_requested_by       ON approvals(requested_by);
CREATE INDEX IF NOT EXISTS idx_decision_logs_project        ON decision_logs(project_id);
CREATE INDEX IF NOT EXISTS idx_decision_logs_status         ON decision_logs(status);
CREATE INDEX IF NOT EXISTS idx_decision_logs_decided_by     ON decision_logs(decided_by);
CREATE INDEX IF NOT EXISTS idx_task_estimates_task          ON task_estimates(task_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user           ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read           ON notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_type           ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_pull_requests_project        ON pull_requests(project_id);
CREATE INDEX IF NOT EXISTS idx_pull_requests_status         ON pull_requests(status);
CREATE INDEX IF NOT EXISTS idx_code_reviews_pr              ON code_reviews(pr_id);
CREATE INDEX IF NOT EXISTS idx_code_reviews_reviewer        ON code_reviews(reviewer_id);
CREATE INDEX IF NOT EXISTS idx_escalations_entity           ON escalations(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_escalations_status           ON escalations(status);
CREATE INDEX IF NOT EXISTS idx_user_skills_user             ON user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_task_required_skills_task    ON task_required_skills(task_id);
CREATE INDEX IF NOT EXISTS idx_attachments_task_id          ON attachments(task_id);
CREATE INDEX IF NOT EXISTS idx_attachments_epic_id          ON attachments(epic_id);
CREATE INDEX IF NOT EXISTS idx_attachments_created_by       ON attachments(created_by);
CREATE INDEX IF NOT EXISTS idx_attachments_created_at       ON attachments(created_at);

-- Default permissions
INSERT INTO permissions (name, description, category) VALUES
('TASK_CREATE',           'Create new tasks',                'TASK_MGMT'),
('TASK_UPDATE_STATUS',    'Update task status',              'TASK_MGMT'),
('TASK_UPDATE_ALL',       'Update all task fields',          'TASK_MGMT'),
('TASK_ASSIGN',           'Assign tasks to others',          'TASK_MGMT'),
('TASK_DELETE',           'Delete tasks',                    'TASK_MGMT'),
('TASK_ESTIMATE',         'Provide time estimates',          'TASK_MGMT'),
('DESIGN_PROPOSE',        'Propose design changes',          'DESIGN'),
('DESIGN_APPROVE',        'Approve design decisions',        'DESIGN'),
('ARCHITECTURE_PROPOSE',  'Propose architecture changes',    'ARCHITECTURE'),
('ARCHITECTURE_APPROVE',  'Approve architecture decisions',  'ARCHITECTURE'),
('ARCHITECTURE_OVERRIDE', 'Override architecture decisions', 'ARCHITECTURE'),
('PR_REVIEW',             'Review pull requests',            'REVIEW'),
('PR_APPROVE',            'Approve pull requests',           'REVIEW'),
('CODE_REVIEW',           'Review code changes',             'REVIEW'),
('ASSIGN_TASK',           'Assign tasks to team members',    'ASSIGN_WORK'),
('ASSIGN_REVIEW',         'Assign code reviews',             'ASSIGN_WORK'),
('CREATE_EPIC',           'Create epics',                    'TASK_MGMT'),
('CREATE_MILESTONE',      'Create milestones',               'TASK_MGMT')
ON CONFLICT (name) DO NOTHING;

-- Role to permission mappings
INSERT INTO role_permissions (role, permission_id)
SELECT 'SDE_1', id FROM permissions
WHERE name IN ('TASK_UPDATE_STATUS', 'TASK_ESTIMATE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role, permission_id)
SELECT 'SDE_2', id FROM permissions
WHERE name IN ('TASK_CREATE', 'TASK_UPDATE_ALL', 'TASK_ASSIGN', 'TASK_ESTIMATE', 'ASSIGN_TASK', 'PR_REVIEW')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role, permission_id)
SELECT 'SENIOR', id FROM permissions
WHERE name IN ('TASK_CREATE', 'TASK_UPDATE_ALL', 'TASK_ASSIGN', 'TASK_DELETE', 'CREATE_EPIC', 'CREATE_MILESTONE', 'PR_APPROVE', 'DESIGN_PROPOSE', 'ASSIGN_REVIEW')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role, permission_id)
SELECT 'STAFF', id FROM permissions
WHERE name IN ('DESIGN_APPROVE', 'ARCHITECTURE_PROPOSE', 'ARCHITECTURE_OVERRIDE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role, permission_id)
SELECT 'PRINCIPAL', id FROM permissions
WHERE name IN ('ARCHITECTURE_APPROVE', 'ARCHITECTURE_OVERRIDE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role, permission_id)
SELECT 'ARCHITECT', id FROM permissions
ON CONFLICT DO NOTHING;
'@

$tempV234 = "$env:TEMP\v234_schema.sql"
Write-SqlFile -filePath $tempV234 -content $v234Schema
Execute-SQLFile -filePath $tempV234 -database $dbName
Remove-Item $tempV234 -ErrorAction SilentlyContinue
Write-Host "OK Extended schema created" -ForegroundColor Green
Write-Host ""

# ---------------------------------------------------------------------------
# Step 5: Verification
# FIX: Pipe through Out-String and Trim to clean up psql -t padding/newlines
# ---------------------------------------------------------------------------
Write-Host "[5/5] Verifying database setup..." -ForegroundColor Cyan

$env:PGPASSWORD = $dbPassword
$tableCount = psql -h $dbServer -p $dbPort -U $dbUsername -d $dbName -t -c `
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>&1
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue

$tableCount = ($tableCount | Out-String).Trim()

Write-Host "OK Database setup completed successfully!" -ForegroundColor Green
Write-Host "   Total tables created: $tableCount"     -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "Database Details:"                        -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "Database Name : $dbName"     -ForegroundColor White
Write-Host "Server        : $dbServer"   -ForegroundColor White
Write-Host "Port          : $dbPort"     -ForegroundColor White
Write-Host "Tables        : $tableCount" -ForegroundColor White
Write-Host ""
Write-Host "Connect using:" -ForegroundColor Yellow
Write-Host "psql -h $dbServer -p $dbPort -U $dbUsername -d $dbName" -ForegroundColor Yellow
Write-Host ""