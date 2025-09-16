-- Batch Types Reference Data
-- This file loads default batch types for the local tenant

INSERT INTO batch_type (name, description, create_time_stamp, update_time_stamp)
SELECT 'FLOWER', 'Flower Batch', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM batch_type WHERE name = 'FLOWER'
);

INSERT INTO batch_type (name, description, create_time_stamp, update_time_stamp)
SELECT 'CONCENTRATE', 'Concentrate Batch', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM batch_type WHERE name = 'CONCENTRATE'
);

INSERT INTO batch_type (name, description, create_time_stamp, update_time_stamp)
SELECT 'EDIBLE', 'Edible Batch', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM batch_type WHERE name = 'EDIBLE'
);