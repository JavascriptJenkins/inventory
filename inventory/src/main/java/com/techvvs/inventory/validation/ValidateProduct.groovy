package com.techvvs.inventory.validation


import com.techvvs.inventory.model.ProductVO
import org.springframework.stereotype.Component

@Component
class ValidateProduct {


    String validateNewFormInfo(ProductVO productVO){

        // todo: implement a price change table so people can change the price of a SKU after a transaction has been made.
        // check if the product price coming in here is different than the existing price in the database

        // if it is indeed different, then we need see if any transactions have been made.
        //
        // If a transaction has indeed been made, then don't let the user change the price


        if(productVO.getName() != null &&
                (productVO.getName().length() > 250
                        || productVO.getName().length() < 1)
        ){
            return "first name must be between 1-250 characters. ";
        }



        if(productVO.getNotes() != null && (productVO.getNotes().length() > 1000)
        ){
            return "Notes must be less than 1000 characters";
        }

        return "success";
    }




}
