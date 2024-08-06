delete from product_cart where cartid > 0
delete from cart where cartid > 0

UPDATE Product
SET quantityremaining = quantity;
