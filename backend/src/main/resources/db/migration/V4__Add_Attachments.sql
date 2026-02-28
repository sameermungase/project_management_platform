-- Create attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

-- Create index for faster queries
CREATE INDEX idx_attachments_task_id ON attachments(task_id);
CREATE INDEX idx_attachments_epic_id ON attachments(epic_id);
CREATE INDEX idx_attachments_created_by ON attachments(created_by);
CREATE INDEX idx_attachments_created_at ON attachments(created_at);
