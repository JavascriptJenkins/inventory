-- Flyway migration to add indexes for request_logs table for performance optimization
-- These indexes will speed up queries for finding top requested URIs by user

-- Index for finding GET requests by user (most common query pattern)
CREATE INDEX IF NOT EXISTS idx_request_logs_http_method_remote_user 
ON request_logs (http_method, remote_user);

-- Index for finding requests by user and URI (for counting frequency)
CREATE INDEX IF NOT EXISTS idx_request_logs_remote_user_request_uri 
ON request_logs (remote_user, request_uri);

-- Index for finding GET requests by user and URI (most specific query)
CREATE INDEX IF NOT EXISTS idx_request_logs_get_user_uri 
ON request_logs (http_method, remote_user, request_uri) 
WHERE http_method = 'GET';

-- Index for timestamp-based queries (for recent activity)
CREATE INDEX IF NOT EXISTS idx_request_logs_timestamp 
ON request_logs (request_timestamp);

-- Composite index for user activity analysis
CREATE INDEX IF NOT EXISTS idx_request_logs_user_timestamp 
ON request_logs (remote_user, request_timestamp);
