package com.techvvs.inventory.validation.generic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class ObjectValidator {


    // Validates the fields of the object and attaches an error message to the model if there are errors
    void validateAndAttachErrors(Object object, Model model) {
        Map<String, String> errors = validateFields(object)

        // If there are errors, concatenate them into a single message and add to the model
        if (!errors.isEmpty()) {
            String errorMessage = errors.values().join(' | ')
            model.addAttribute("errorMessage", errorMessage)
        }
    }


    Map<String, String> validateFields(Object object) {
        Map<String, String> errors = [:]

        // Check each field only if it exists on the object
        if (object.hasProperty('name') && (!object.name || object.name.trim().isEmpty())) {
            errors['name'] = 'Name cannot be empty.'
        }

        if (object.hasProperty('description') && (!object.description || object.description.trim().isEmpty())) {
            errors['description'] = 'Description cannot be empty.'
        }

        if (object.hasProperty('address') && (!object.address || object.address.trim().isEmpty())) {
            errors['address'] = 'Address cannot be empty.'
        }

        if (object.hasProperty('address2') && object.address2 && object.address2.size() > 100) {
            errors['address2'] = 'Address2 should be less than 100 characters.'
        }

        if (object.hasProperty('city') && (!object.city || object.city.trim().isEmpty())) {
            errors['city'] = 'City cannot be empty.'
        }

        if (object.hasProperty('state')) {
            if (!object.state || object.state.trim().isEmpty()) {
                errors['state'] = 'State cannot be empty.'
            } else if (object.state.size() != 2) {
                errors['state'] = 'State must be a 2-letter code.'
            }
        }

        if (object.hasProperty('zipcode')) {
            if (!object.zipcode || !object.zipcode.isNumber() || object.zipcode.size() != 5) {
                errors['zipcode'] = 'Zipcode must be a 5-digit number.'
            }
        }

        return errors
    }
}
