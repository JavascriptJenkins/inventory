-- Package Types Reference Data
-- This file loads default package types for the local tenant

INSERT INTO package_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'LARGE.BOX', 'A large box used for shipping', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM package_type_vo WHERE name = 'LARGE.BOX'
);

INSERT INTO package_type_vo (name, description, create_time_stamp, update_time_stamp)
SELECT 'SMALL.BOX', 'A small box used for shipping', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM package_type_vo WHERE name = 'SMALL.BOX'
);
