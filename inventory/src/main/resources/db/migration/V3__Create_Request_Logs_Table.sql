-- Flyway migration to create request_logs table for tracking HTTP request metadata

CREATE TABLE IF NOT EXISTS request_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Basic request information
    request_timestamp TIMESTAMP NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_uri VARCHAR(2000) NOT NULL,
    query_string VARCHAR(2000),
    protocol VARCHAR(20),
    
    -- Client information
    client_ip VARCHAR(45),
    client_host VARCHAR(255),
    client_port INTEGER,
    
    -- Server information
    server_name VARCHAR(255),
    server_port INTEGER,
    context_path VARCHAR(500),
    servlet_path VARCHAR(500),
    path_info VARCHAR(500),
    
    -- User agent and device information
    user_agent VARCHAR(2000),
    browser VARCHAR(100),
    browser_version VARCHAR(50),
    operating_system VARCHAR(100),
    os_version VARCHAR(50),
    device_type VARCHAR(20),
    device_model VARCHAR(100),
    is_bot BOOLEAN,
    is_touch_device BOOLEAN,
    is_64bit BOOLEAN,
    
    -- Content information
    content_type VARCHAR(255),
    content_length BIGINT,
    character_encoding VARCHAR(50),
    
    -- Session information
    session_id VARCHAR(100),
    requested_session_id VARCHAR(100),
    session_from_cookie BOOLEAN,
    session_from_url BOOLEAN,
    session_valid BOOLEAN,
    
    -- Locale information
    locale VARCHAR(20),
    accept_language VARCHAR(500),
    
    -- Security information
    is_secure BOOLEAN,
    auth_type VARCHAR(50),
    remote_user VARCHAR(255),
    referer VARCHAR(2000),
    origin VARCHAR(500),
    
    -- Response information
    response_timestamp TIMESTAMP,
    response_status INTEGER,
    response_content_type VARCHAR(255),
    response_character_encoding VARCHAR(50),
    
    -- Request duration in milliseconds
    duration_ms BIGINT,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_request_logs_timestamp ON request_logs(request_timestamp);
CREATE INDEX IF NOT EXISTS idx_request_logs_method ON request_logs(http_method);
CREATE INDEX IF NOT EXISTS idx_request_logs_uri ON request_logs(request_uri);
CREATE INDEX IF NOT EXISTS idx_request_logs_client_ip ON request_logs(client_ip);
CREATE INDEX IF NOT EXISTS idx_request_logs_browser ON request_logs(browser);
CREATE INDEX IF NOT EXISTS idx_request_logs_os ON request_logs(operating_system);
CREATE INDEX IF NOT EXISTS idx_request_logs_device_type ON request_logs(device_type);
CREATE INDEX IF NOT EXISTS idx_request_logs_is_bot ON request_logs(is_bot);
CREATE INDEX IF NOT EXISTS idx_request_logs_response_status ON request_logs(response_status);
CREATE INDEX IF NOT EXISTS idx_request_logs_session_id ON request_logs(session_id);
CREATE INDEX IF NOT EXISTS idx_request_logs_created_at ON request_logs(created_at);

-- Create composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_request_logs_method_uri ON request_logs(http_method, request_uri);
CREATE INDEX IF NOT EXISTS idx_request_logs_timestamp_status ON request_logs(request_timestamp, response_status);
CREATE INDEX IF NOT EXISTS idx_request_logs_client_ip_timestamp ON request_logs(client_ip, request_timestamp);

-- Add comments for documentation
COMMENT ON TABLE request_logs IS 'Table to store HTTP request metadata for monitoring and analytics';
COMMENT ON COLUMN request_logs.id IS 'Primary key UUID';
COMMENT ON COLUMN request_logs.request_timestamp IS 'Timestamp when the request was received';
COMMENT ON COLUMN request_logs.http_method IS 'HTTP method (GET, POST, PUT, DELETE, etc.)';
COMMENT ON COLUMN request_logs.request_uri IS 'Request URI path';
COMMENT ON COLUMN request_logs.query_string IS 'Query string parameters';
COMMENT ON COLUMN request_logs.client_ip IS 'Client IP address (with proxy support)';
COMMENT ON COLUMN request_logs.user_agent IS 'User-Agent header value';
COMMENT ON COLUMN request_logs.browser IS 'Detected browser name';
COMMENT ON COLUMN request_logs.operating_system IS 'Detected operating system';
COMMENT ON COLUMN request_logs.device_type IS 'Device type (Mobile, Tablet, Desktop)';
COMMENT ON COLUMN request_logs.is_bot IS 'Whether the request is from a bot/crawler';
COMMENT ON COLUMN request_logs.response_status IS 'HTTP response status code';
COMMENT ON COLUMN request_logs.duration_ms IS 'Request duration in milliseconds';
COMMENT ON COLUMN request_logs.session_id IS 'Session ID if available';
COMMENT ON COLUMN request_logs.is_secure IS 'Whether the request was made over HTTPS';

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_request_logs_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_request_logs_updated_at
    BEFORE UPDATE ON request_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_request_logs_updated_at();





