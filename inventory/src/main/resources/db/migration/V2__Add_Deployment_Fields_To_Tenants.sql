-- Add deployment-related fields to tenants table
-- Migration: V2__Add_Deployment_Fields_To_Tenants.sql

-- Add last_deployed column (timestamp for when tenant was last deployed)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS last_deployed TIMESTAMP;

-- Add deploy_flag column (integer flag for deployment status)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS deploy_flag INTEGER DEFAULT 0;

-- Add comment to explain the deploy_flag values
COMMENT ON COLUMN tenants.deploy_flag IS 'Deployment flag: 0=not deployed, 1=deployed, 2=deployment in progress, 3=deployment failed';

-- Add comment to explain the last_deployed field
COMMENT ON COLUMN tenants.last_deployed IS 'Timestamp of the last successful deployment of this tenant';





