package com.techvvs.inventory.validation

import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import org.springframework.stereotype.Component

@Component
class ValidateBatch {


    String validateNewFormInfo(BatchVO batchVO){

        if(batchVO.getName() != null &&
                (batchVO.getName().length() > 250
                        || batchVO.getName().length() < 1)
        ){
            return "first name must be between 1-250 characters. ";
        }



        if(batchVO.getNotes() != null && (batchVO.getNotes().length() > 1000)
        ){
            return "Notes must be less than 1000 characters";
        }

        return "success";
    }




}
