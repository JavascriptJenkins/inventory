package com.techvvs.inventory.validation

import com.techvvs.inventory.model.PackageTypeVO
import org.springframework.stereotype.Component

@Component
class ValidatePackageType {


    String validateNewFormInfo(PackageTypeVO packageTypeVO){

        if(packageTypeVO.getName() != null &&
                (packageTypeVO.getName().length() > 250
                        || packageTypeVO.getName().length() < 1)
        ){
            return "Name must be between 1-250 characters. ";
        }

        if(packageTypeVO.getDescription() != null && (packageTypeVO.getDescription().length() > 1000)
        ){
            return "Description must be less than 1000 characters";
        }

        return "success";
    }




}
