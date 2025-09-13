-- Create Tenants table
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY,
    tenant_name VARCHAR(50) UNIQUE NOT NULL,
    domain_name VARCHAR(100),
    subscription_tier VARCHAR(20),
    status VARCHAR(20) DEFAULT 'pending', -- active, pending, suspended
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    billing_email VARCHAR(255),
    updateTimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    createTimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Tenant configurations table
CREATE TABLE IF NOT EXISTS tenant_configs (
    id UUID PRIMARY KEY,
    tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    updateTimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    createTimeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, config_key)
);

-- Add tenant_id column to systemuser table if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'systemuser' AND column_name = 'tenant_id') THEN
        ALTER TABLE systemuser ADD COLUMN tenant_id UUID REFERENCES tenants(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tenants_tenant_name ON tenants(tenant_name);
CREATE INDEX IF NOT EXISTS idx_tenants_domain_name ON tenants(domain_name);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants(status);
CREATE INDEX IF NOT EXISTS idx_tenants_subscription_tier ON tenants(subscription_tier);
CREATE INDEX IF NOT EXISTS idx_tenant_configs_tenant_id ON tenant_configs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_configs_config_key ON tenant_configs(config_key);
CREATE INDEX IF NOT EXISTS idx_systemuser_tenant_id ON systemuser(tenant_id);

-- Create triggers to automatically update timestamps
CREATE OR REPLACE FUNCTION update_updated_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updateTimeStamp = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to tenants table
DROP TRIGGER IF EXISTS update_tenants_updated_timestamp ON tenants;
CREATE TRIGGER update_tenants_updated_timestamp
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_timestamp();

-- Apply triggers to tenant_configs table
DROP TRIGGER IF EXISTS update_tenant_configs_updated_timestamp ON tenant_configs;
CREATE TRIGGER update_tenant_configs_updated_timestamp
    BEFORE UPDATE ON tenant_configs
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_timestamp();

-- Insert some sample data (optional)
INSERT INTO tenants (tenant_name, domain_name, subscription_tier, status, billing_email) 
VALUES 
    ('demo', 'demo.techvvs.io', 'BASIC', 'active', 'demo@techvvs.io'),
    ('test', 'test.techvvs.io', 'PREMIUM', 'active', 'test@techvvs.io')
ON CONFLICT (tenant_name) DO NOTHING;

-- Insert some sample tenant configurations
INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT 
    t.id,
    'max_users',
    CASE 
        WHEN t.subscription_tier = 'BASIC' THEN '10'
        WHEN t.subscription_tier = 'PREMIUM' THEN '50'
        WHEN t.subscription_tier = 'ENTERPRISE' THEN 'unlimited'
        ELSE '5'
    END
FROM tenants t
WHERE t.tenant_name IN ('demo', 'test')
ON CONFLICT (tenant_id, config_key) DO NOTHING;

INSERT INTO tenant_configs (tenant_id, config_key, config_value)
SELECT 
    t.id,
    'storage_limit_gb',
    CASE 
        WHEN t.subscription_tier = 'BASIC' THEN '10'
        WHEN t.subscription_tier = 'PREMIUM' THEN '100'
        WHEN t.subscription_tier = 'ENTERPRISE' THEN '1000'
        ELSE '1'
    END
FROM tenants t
WHERE t.tenant_name IN ('demo', 'test')
ON CONFLICT (tenant_id, config_key) DO NOTHING;
