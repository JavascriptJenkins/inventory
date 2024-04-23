package com.techvvs.inventory.validation

import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.ProductTypeVO
import org.springframework.stereotype.Component

@Component
class ValidateProductType {


    String validateNewFormInfo(ProductTypeVO productTypeVO){

        if(productTypeVO.getName() != null &&
                (productTypeVO.getName().length() > 250
                        || productTypeVO.getName().length() < 1)
        ){
            return "Name must be between 1-250 characters. ";
        }

        if(productTypeVO.getDescription() != null && (productTypeVO.getDescription().length() > 1000)
        ){
            return "Description must be less than 1000 characters";
        }

        return "success";
    }




}
