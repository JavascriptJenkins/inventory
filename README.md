# inventory

## ALPHA TODO CHECKLIST:
    1) Implement a Tenant Table that is tied to the SystemUser
    2) Magic Login Link over email
    3) Price Change Table so people can change prices ater a transaction has executed
    4) Integrate some Payment Gateways (venmo, google pay, cash app, paypal, coinbase)
    5) Make sure all fields have correct javascript validation
    6) Investigate/Fix the quantity bug when creating new products
    7) Make more buttons for "accessing last item edited" etc. (last menu, etc)
    8) Make video encoding correct format for partial load
    9) Clean up media viewing on the UI
    10) Make a Terms of Service to keep compliance with payment gateways and apis
    11) Add vendor dropdown to the edit product ui along with fancyform validation
    12) Add default product types for each industry type

## 

    Note: Read these deployment notes before pushing to prod.  If you are pushing to prod 
    after this date, then you must do what it says here before deploying.

    Deployment notes:
    - 1/21/25: update length of token.token
    ALTER TABLE token ALTER COLUMN token TYPE VARCHAR(1024);
    ALTER TABLE customer ALTER COLUMN shoppingtoken TYPE VARCHAR(1024);

    SELECT character_maximum_length
    FROM information_schema.columns
    WHERE table_name = 'token' AND column_name = 'token';

    - 1/21/25
    - When deploying to test realized we need to have the column 
    "ispickup" on the delivery table.  Datatype Number.  0 or 1
    we have to set all of the deliveries to 0
    - probably should just delete all the existing deliveries before
    deploying
    - need to delete any deliveries connected to a customer record too

    - Caused by: org.thymeleaf.exceptions.TemplateProcessingException: Exception evaluating SpringEL expression: "delivery.transaction.customervo.name" (template: "delivery/deliveryqueue.html" - line 294, col 17)

    - 1/23/25
    - Make sure to load the ref data so we have the new location types

    - 6/19/25
    org.hibernate.tool.schema.spi.CommandAcceptanceException: Error executing DDL "alter table discount add column quantity int4 not null" via JDBC Statement

    ALTER TABLE discount ADD COLUMN quantity int4 NOT NULL DEFAULT 1;

