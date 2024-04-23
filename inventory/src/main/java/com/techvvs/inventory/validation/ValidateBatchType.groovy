package com.techvvs.inventory.validation

import com.techvvs.inventory.model.BatchTypeVO
import org.springframework.stereotype.Component

@Component
class ValidateBatchType {


    String validateNewFormInfo(BatchTypeVO batchTypeVO){

        if(batchTypeVO.getName() != null &&
                (batchTypeVO.getName().length() > 250
                        || batchTypeVO.getName().length() < 1)
        ){
            return "Name must be between 1-250 characters. ";
        }

        if(batchTypeVO.getDescription() != null && (batchTypeVO.getDescription().length() > 1000)
        ){
            return "Description must be less than 1000 characters";
        }

        return "success";
    }





}
