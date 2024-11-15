package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class LocationHelper {

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    LocationVO validateLocation(LocationVO locationVO, Model model) {
        stringSecurityValidator.validateStringValues(locationVO, model)
        objectValidator.validateAndAttachErrors(locationVO, model)

        return locationVO;
    }

}
