-- Add user roles table for secure role management
CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(255) PRIMARY KEY,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(255),
    notes TEXT,
    CONSTRAINT valid_role CHECK (role IN ('USER', 'ADMIN', 'MODERATOR'))
);

-- Create index for faster lookups
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- Add sample admin user (replace with your actual Clerk user ID)
-- You can add your user ID here after deployment
-- INSERT INTO user_roles (user_id, role, granted_by, notes) 
-- VALUES ('user_xxxxxxxxxxxxx', 'ADMIN', 'system', 'Initial admin user');
