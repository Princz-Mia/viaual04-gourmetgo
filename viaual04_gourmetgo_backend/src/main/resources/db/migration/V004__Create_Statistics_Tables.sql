-- Create visit statistics table
CREATE TABLE visit_statistics (
    id UUID PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    total_visits BIGINT NOT NULL DEFAULT 0,
    unique_visitors BIGINT NOT NULL DEFAULT 0,
    authenticated_users BIGINT NOT NULL DEFAULT 0,
    anonymous_users BIGINT NOT NULL DEFAULT 0
);

-- Create request logs table
CREATE TABLE request_logs (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INTEGER NOT NULL,
    response_time BIGINT NOT NULL,
    user_agent TEXT,
    ip_address VARCHAR(45)
);

-- Create active sessions table
CREATE TABLE active_sessions (
    id UUID PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    last_activity TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Create indexes for better performance
CREATE INDEX idx_visit_statistics_date ON visit_statistics(date);
CREATE INDEX idx_request_logs_timestamp ON request_logs(timestamp);
CREATE INDEX idx_request_logs_endpoint ON request_logs(endpoint);
CREATE INDEX idx_active_sessions_last_activity ON active_sessions(last_activity);
CREATE INDEX idx_active_sessions_session_id ON active_sessions(session_id);

-- Add created_at column to user table for registration statistics
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;