-- Location Types Reference Data
-- This file loads default location types for the local tenant

INSERT INTO location_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'RETAIL', 'Retail Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM location_type_vo WHERE name = 'B2B.DISTRO'
);

INSERT INTO location_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'WHOLESALE', 'Wholesale Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM location_type_vo WHERE name = 'B2C.RETAIL'
);

INSERT INTO location_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'DELIVERY', 'Delivery Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM location_type_vo WHERE name = 'B2C.RETAIL'
);

INSERT INTO location_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'CULTIVATION', 'Cultivation Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM location_type_vo WHERE name = 'B2C.RETAIL'
);

INSERT INTO location_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'TRANSPORT', 'Transport Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM location_type_vo WHERE name = 'B2C.RETAIL'
);

INSERT INTO location_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'MANUFACTURING', 'Manufacturing Location', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM location_type_vo WHERE name = 'B2C.RETAIL'
);