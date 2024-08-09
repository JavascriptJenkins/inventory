-- Update Customer with customerid = 7
UPDATE Customer
SET
    address = 'New Address 1',
    address2 = 'New Address2 1',
    email = 'newemail1@example.com',
    name = 'New Name 1',
    notes = 'Updated notes for customer 1',
    phone = '123-456-7890'
WHERE customerid = 7;

-- Update Customer with customerid = 9
UPDATE Customer
SET
    address = 'New Address 2',
    address2 = 'New Address2 2',
    email = 'newemail2@example.com',
    name = 'New Name 2',
    notes = 'Updated notes for customer 2',
    phone = '234-567-8901'
WHERE customerid = 8;

-- Update Customer with customerid = 810
UPDATE Customer
SET
    address = 'New Address 3',
    address2 = 'New Address2 3',
    email = 'newemail3@example.com',
    name = 'New Name 3',
    notes = 'Updated notes for customer 3',
    phone = '345-678-9012'
WHERE customerid = 810;
