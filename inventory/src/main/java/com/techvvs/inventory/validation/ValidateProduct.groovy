package com.techvvs.inventory.validation


import com.techvvs.inventory.model.ProductVO
import org.springframework.stereotype.Component

@Component
class ValidateProduct {


    String validateNewFormInfo(ProductVO productVO){

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
