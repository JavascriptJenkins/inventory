-- Product Types Reference Data for LOCAL tenant
-- This file loads default product types for the local tenant

INSERT INTO product_type (name, description, create_time_stamp, update_time_stamp)
SELECT 'FLOWER', 'Flower', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM product_type WHERE name = 'FLOWER'
);

INSERT INTO product_type (name, description, create_time_stamp, update_time_stamp)
SELECT 'CONCENTRATE', 'Concentrate', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM product_type WHERE name = 'CONCENTRATE'
);

INSERT INTO product_type (name, description, create_time_stamp, update_time_stamp)
SELECT 'EDIBLE', 'Edible', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM product_type WHERE name = 'EDIBLE'
);