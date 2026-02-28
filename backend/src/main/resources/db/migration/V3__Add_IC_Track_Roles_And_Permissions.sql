-- =====================================================
-- IC Track Role & Authority Model - Database Migration
-- =====================================================

-- 1. Project Role Assignment Table (Project-specific roles)
CREATE TABLE project_member_roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL, -- SDE_1, SDE_2, SENIOR, STAFF, PRINCIPAL, ARCHITECT
    assigned_by UUID,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(project_id, user_id, role)
);

-- 2. Permissions Table (Granular permissions)
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) -- TASK_MGMT, DESIGN, REVIEW, ASSIGN_WORK, ARCHITECTURE
);

-- 3. Role Permissions Mapping (Which roles have which permissions)
CREATE TABLE role_permissions (
    role VARCHAR(50) NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 4. Epics Table
CREATE TABLE epics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PLANNING', -- PLANNING, IN_PROGRESS, COMPLETED, CANCELLED
    visibility_level VARCHAR(20) DEFAULT 'TEAM', -- PUBLIC, TEAM, SENIOR_PLUS, STAFF_PLUS, PRINCIPAL_PLUS
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Milestones Table
CREATE TABLE milestones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    epic_id UUID, -- Optional: milestone can belong to an epic
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_date DATE,
    status VARCHAR(20) DEFAULT 'PLANNED', -- PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (epic_id) REFERENCES epics(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Link Tasks to Epics and Milestones
ALTER TABLE tasks ADD COLUMN epic_id UUID;
ALTER TABLE tasks ADD COLUMN milestone_id UUID;
ALTER TABLE tasks ADD COLUMN visibility_level VARCHAR(20) DEFAULT 'PUBLIC';
ALTER TABLE tasks ADD FOREIGN KEY (epic_id) REFERENCES epics(id) ON DELETE SET NULL;
ALTER TABLE tasks ADD FOREIGN KEY (milestone_id) REFERENCES milestones(id) ON DELETE SET NULL;

-- 7. Approval Workflows Table
CREATE TABLE approvals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL, -- TASK, EPIC, MILESTONE, DESIGN_DECISION, ARCHITECTURE_CHANGE
    entity_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, CANCELLED
    approved_by UUID,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    comments TEXT,
    FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 8. Decision Logs Table (Audit trail for technical decisions)
CREATE TABLE decision_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    decision_type VARCHAR(50), -- ARCHITECTURE, DESIGN_PATTERN, TECH_STACK, PROCESS
    decided_by UUID NOT NULL,
    approved_by UUID, -- For decisions requiring approval
    status VARCHAR(20) DEFAULT 'PROPOSED', -- PROPOSED, APPROVED, REJECTED, IMPLEMENTED
    visibility_level VARCHAR(20) DEFAULT 'SENIOR_PLUS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (decided_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 9. Task Estimates Table (Timeline estimation tracking)
CREATE TABLE task_estimates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    estimated_by UUID NOT NULL,
    estimated_hours DECIMAL(10,2),
    estimated_days INTEGER,
    confidence_level VARCHAR(20), -- LOW, MEDIUM, HIGH
    estimation_type VARCHAR(20) DEFAULT 'INITIAL', -- INITIAL, REVISED, ACTUAL
    reason TEXT, -- Why estimate changed
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (estimated_by) REFERENCES users(id) ON DELETE CASCADE
);

-- 10. Notifications Table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL, -- APPROVAL_REQUEST, ROLE_CHANGE, TASK_ASSIGNED, DECISION_APPROVED, ESCALATION
    title VARCHAR(200) NOT NULL,
    message TEXT,
    entity_type VARCHAR(50),
    entity_id UUID,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 11. Pull Requests / Code Reviews Table
CREATE TABLE pull_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    source_branch VARCHAR(100),
    target_branch VARCHAR(100),
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, REVIEWING, APPROVED, MERGED, CLOSED
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

-- 12. Code Reviews Table
CREATE TABLE code_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pr_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, CHANGES_REQUESTED
    comments TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pr_id) REFERENCES pull_requests(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 13. Escalations Table
CREATE TABLE escalations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    escalated_by UUID NOT NULL,
    escalated_to UUID NOT NULL, -- Higher authority
    reason TEXT,
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, RESOLVED, DISMISSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    FOREIGN KEY (escalated_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (escalated_to) REFERENCES users(id) ON DELETE CASCADE
);

-- 14. User Skills Table
CREATE TABLE user_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    proficiency_level VARCHAR(20), -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, skill_name)
);

-- 15. Task Required Skills Table
CREATE TABLE task_required_skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    UNIQUE(task_id, skill_name)
);

-- Indexes for performance
CREATE INDEX idx_project_member_roles_project ON project_member_roles(project_id);
CREATE INDEX idx_project_member_roles_user ON project_member_roles(user_id);
CREATE INDEX idx_epics_project ON epics(project_id);
CREATE INDEX idx_epics_status ON epics(status);
CREATE INDEX idx_milestones_project ON milestones(project_id);
CREATE INDEX idx_milestones_epic ON milestones(epic_id);
CREATE INDEX idx_milestones_status ON milestones(status);
CREATE INDEX idx_tasks_epic ON tasks(epic_id);
CREATE INDEX idx_tasks_milestone ON tasks(milestone_id);
CREATE INDEX idx_approvals_entity ON approvals(entity_type, entity_id);
CREATE INDEX idx_approvals_status ON approvals(status);
CREATE INDEX idx_approvals_requested_by ON approvals(requested_by);
CREATE INDEX idx_decision_logs_project ON decision_logs(project_id);
CREATE INDEX idx_decision_logs_status ON decision_logs(status);
CREATE INDEX idx_decision_logs_decided_by ON decision_logs(decided_by);
CREATE INDEX idx_task_estimates_task ON task_estimates(task_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_pull_requests_project ON pull_requests(project_id);
CREATE INDEX idx_pull_requests_status ON pull_requests(status);
CREATE INDEX idx_code_reviews_pr ON code_reviews(pr_id);
CREATE INDEX idx_code_reviews_reviewer ON code_reviews(reviewer_id);
CREATE INDEX idx_escalations_entity ON escalations(entity_type, entity_id);
CREATE INDEX idx_escalations_status ON escalations(status);
CREATE INDEX idx_user_skills_user ON user_skills(user_id);
CREATE INDEX idx_task_required_skills_task ON task_required_skills(task_id);

-- Insert default permissions
INSERT INTO permissions (name, description, category) VALUES
-- Task Management Permissions
('TASK_CREATE', 'Create new tasks', 'TASK_MGMT'),
('TASK_UPDATE_STATUS', 'Update task status', 'TASK_MGMT'),
('TASK_UPDATE_ALL', 'Update all task fields', 'TASK_MGMT'),
('TASK_ASSIGN', 'Assign tasks to others', 'TASK_MGMT'),
('TASK_DELETE', 'Delete tasks', 'TASK_MGMT'),
('TASK_ESTIMATE', 'Provide time estimates', 'TASK_MGMT'),

-- Design & Architecture Permissions
('DESIGN_PROPOSE', 'Propose design changes', 'DESIGN'),
('DESIGN_APPROVE', 'Approve design decisions', 'DESIGN'),
('ARCHITECTURE_PROPOSE', 'Propose architecture changes', 'ARCHITECTURE'),
('ARCHITECTURE_APPROVE', 'Approve architecture decisions', 'ARCHITECTURE'),
('ARCHITECTURE_OVERRIDE', 'Override architecture decisions', 'ARCHITECTURE'),

-- Review Permissions
('PR_REVIEW', 'Review pull requests', 'REVIEW'),
('PR_APPROVE', 'Approve pull requests', 'REVIEW'),
('CODE_REVIEW', 'Review code changes', 'REVIEW'),

-- Work Assignment Permissions
('ASSIGN_TASK', 'Assign tasks to team members', 'ASSIGN_WORK'),
('ASSIGN_REVIEW', 'Assign code reviews', 'ASSIGN_WORK'),
('CREATE_EPIC', 'Create epics', 'TASK_MGMT'),
('CREATE_MILESTONE', 'Create milestones', 'TASK_MGMT');

-- Map permissions to roles
-- SDE-1 Permissions
INSERT INTO role_permissions (role, permission_id) 
SELECT 'SDE_1', id FROM permissions 
WHERE name IN ('TASK_UPDATE_STATUS', 'TASK_ESTIMATE');

-- SDE-2 Permissions
INSERT INTO role_permissions (role, permission_id) 
SELECT 'SDE_2', id FROM permissions 
WHERE name IN ('TASK_CREATE', 'TASK_UPDATE_ALL', 'TASK_ASSIGN', 'TASK_ESTIMATE', 'ASSIGN_TASK', 'PR_REVIEW');

-- Senior Permissions
INSERT INTO role_permissions (role, permission_id) 
SELECT 'SENIOR', id FROM permissions 
WHERE name IN ('TASK_CREATE', 'TASK_UPDATE_ALL', 'TASK_ASSIGN', 'TASK_DELETE', 
                'CREATE_EPIC', 'CREATE_MILESTONE', 'PR_APPROVE', 'DESIGN_PROPOSE', 'ASSIGN_REVIEW');

-- Staff Permissions
INSERT INTO role_permissions (role, permission_id) 
SELECT 'STAFF', id FROM permissions 
WHERE name IN ('DESIGN_APPROVE', 'ARCHITECTURE_PROPOSE', 'ARCHITECTURE_OVERRIDE');

-- Principal Permissions
INSERT INTO role_permissions (role, permission_id) 
SELECT 'PRINCIPAL', id FROM permissions 
WHERE name IN ('ARCHITECTURE_APPROVE', 'ARCHITECTURE_OVERRIDE');

-- Architect Permissions (All)
INSERT INTO role_permissions (role, permission_id) 
SELECT 'ARCHITECT', id FROM permissions;
