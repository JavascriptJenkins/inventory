# inventory

## 

    Note: Read these deployment notes before pushing to prod.  If you are pushing to prod 
    after this date, then you must do what it says here before deploying.

    Deployment notes:
    - 1/21/25: update length of token.token
    ALTER TABLE token ALTER COLUMN token TYPE VARCHAR(1024);
