# inventory

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

