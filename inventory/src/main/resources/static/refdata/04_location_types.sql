-- Location Types Reference Data
-- This file loads default location types for the local tenant

INSERT INTO locationtype (name, description, create_time_stamp, update_time_stamp)
SELECT 'RETAIL', 'Retail Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM locationtype WHERE name = 'RETAIL'
);

INSERT INTO locationtype (name, description, create_time_stamp, update_time_stamp)
SELECT 'WHOLESALE', 'Wholesale Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM locationtype WHERE name = 'WHOLESALE'
);

INSERT INTO locationtype (name, description, create_time_stamp, update_time_stamp)
SELECT 'DELIVERY', 'Delivery Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM locationtype WHERE name = 'DELIVERY'
);

INSERT INTO locationtype (name, description, create_time_stamp, update_time_stamp)
SELECT 'CULTIVATION', 'Cultivation Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM locationtype WHERE name = 'CULTIVATION'
);

INSERT INTO locationtype (name, description, create_time_stamp, update_time_stamp)
SELECT 'TRANSPORT', 'Transport Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM locationtype WHERE name = 'TRANSPORT'
);

INSERT INTO locationtype (name, description, create_time_stamp, update_time_stamp)
SELECT 'MANUFACTURING', 'Manufacturing Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM locationtype WHERE name = 'MANUFACTURING'
);