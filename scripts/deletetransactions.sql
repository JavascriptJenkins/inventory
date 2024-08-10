delete from TRANSACTION_PAYMENT_LIST  where TRANSACTIONVO_TRANSACTIONID > 0
delete from TRANSACTION_PRODUCT   where TRANSACTIONID > 0
delete from PAYMENT   where TRANSACTIONID > 0
delete from TRANSACTION   where TRANSACTIONID > 0



delete from product_cart where cartid > 0
delete from cart where cartid > 0


UPDATE Product
SET quantityremaining = quantity;

UPDATE Product
SET quantityremaining = 2 where product_id = 12;