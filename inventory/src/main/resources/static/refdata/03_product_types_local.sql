-- Product Types Reference Data for LOCAL tenant
-- This file loads default product types for the local tenant

INSERT INTO product_type (name, description, createTimeStamp, updateTimeStamp)
SELECT 'FLOWER', 'Flower', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM product_type WHERE name = 'FLOWER'
);

INSERT INTO product_type (name, description, createTimeStamp, updateTimeStamp)
SELECT 'CONCENTRATE', 'Concentrate', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM product_type WHERE name = 'CONCENTRATE'
);

INSERT INTO product_type (name, description, createTimeStamp, updateTimeStamp)
SELECT 'EDIBLE', 'Edible', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM product_type WHERE name = 'EDIBLE'
);