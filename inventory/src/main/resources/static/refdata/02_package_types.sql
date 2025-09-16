-- Package Types Reference Data
-- This file loads default package types for the local tenant

INSERT INTO packagetype (name, description, createTimeStamp, updateTimeStamp)
SELECT 'LARGE.BOX', 'A large box used for shipping', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM packagetype WHERE name = 'LARGE.BOX'
);

INSERT INTO packagetype (name, description, createTimeStamp, updateTimeStamp)
SELECT 'SMALL.BOX', 'A small box used for shipping', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM packagetype WHERE name = 'SMALL.BOX'
);
