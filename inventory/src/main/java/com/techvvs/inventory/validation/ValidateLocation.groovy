package com.techvvs.inventory.validation

import com.techvvs.inventory.model.LocationVO
import org.springframework.stereotype.Component

@Component
class ValidateLocation {


    String validateNewFormInfo(LocationVO locationVO){

        if(locationVO.getName() != null &&
                (locationVO.getName().length() > 250
                        || locationVO.getName().length() < 1)
        ){
            return "Name must be between 1-250 characters. ";
        }

        if(locationVO.getDescription() != null && (locationVO.getDescription().length() > 1000)
        ){
            return "Description must be less than 1000 characters";
        }

        return "success";
    }




}
