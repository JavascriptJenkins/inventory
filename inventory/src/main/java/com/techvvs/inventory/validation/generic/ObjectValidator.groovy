package com.techvvs.inventory.validation.generic

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.util.ModelMessageUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class ObjectValidator {

    @Autowired
    ModelMessageUtil modelMessageUtil


    void validateForCreateOrEdit(Object object, Model model, boolean isCreate) {
        if(isCreate) {
            validateAndAttachErrorsForCreate(object, model)
        } else {
            validateAndAttachErrors(object, model)
        }
    }


    // Validates the fields of the object and attaches an error message to the model if there are errors
    void validateAndAttachErrors(Object object, Model model) {
        Map<String, String> errors = validateFields(object)

        // If there are errors, concatenate them into a single message and add to the model
        if (!errors.isEmpty()) {
            String errorMessage = errors.values().join(' | ')
            modelMessageUtil.addMessage(model, MessageConstants.ERROR_MSG, errorMessage)
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

        // added fields below here for productVO
        if (object.hasProperty('quantity')) {
            if (object.quantity == null || object.quantity < 0 || object.quantity > 999999) {
                errors['quantity'] = 'Quantity must be an integer between 0 and 999999.'
            }
        }

        if (object.hasProperty('quantityremaining')) {
            if (object.quantityremaining == null || object.quantityremaining < 0 || object.quantityremaining > 999999) {
                errors['quantityremaining'] = 'Quantity Remaining must be an integer between 0 and 999999.'
            }
        }

        if (object.hasProperty('price')) {
            if (object.price == null || object.price < 0 || object.price >= 999999) {
                errors['price'] = 'Price must be a number between 0 and 999999.'
            }
        }

        if (object.hasProperty('cost')) {
            if (object.cost == null || object.cost < 0 || object.cost >= 999999) {
                errors['cost'] = 'Cost must be a number between 0 and 999999.'
            }
        }

//        /* Ensure updateTimeStamp and createTimeStamp are not null */
//        if (object.hasProperty('updateTimeStamp')) {
//            if (object.updateTimeStamp == null) {
//                errors['updateTimeStamp'] = 'Update TimeStamp cannot be empty.'
//            }
//        }
//
//        if (object.hasProperty('createTimeStamp')) {
//            if (object.createTimeStamp == null) {
//                errors['createTimeStamp'] = 'Create TimeStamp cannot be empty.'
//            }
//        }

        /* Ensure productnumber is an integer and not null */
        if (object.hasProperty('productnumber')) {
            if (object.productnumber == null) {
                errors['productnumber'] = 'Product Number cannot be empty.'
            } else if (!(object.productnumber instanceof Integer)) {
                errors['productnumber'] = 'Product Number must be an integer.'
            }
        }

        /* Ensure producttypeid is not null */
        if (object.hasProperty('producttypeid')) {
            if (object.producttypeid == null) {
                errors['producttypeid'] = 'Product Type ID cannot be null.'
            }
        }

//        /* Ensure barcode is exactly 12 digits, an integer, and not null */
//        if (object.hasProperty('barcode')) {
//            if (object.barcode == null || object.barcode.trim().isEmpty()) {
//                errors['barcode'] = 'Barcode cannot be empty.'
//            } else if (!object.barcode.matches("\\d{12}")) {
//                errors['barcode'] = 'Barcode must be exactly 12 digits long and numeric.'
//            }
//
//        }

        /* Ensure vendor is a String and has at least 2 characters */
//        if (object.hasProperty('vendor')) {
//            def vendor = object.vendor
//
//            if (vendor == null) {
//                errors['vendor'] = 'Vendor must be selected.'
//            } else if (vendor instanceof String) {
//                if (vendor.trim().length() <= 1) {
//                    errors['vendor'] = 'Vendor must be a valid string with at least 2 characters.'
//                }
//            } else if (vendor.respondsTo('getVendorid')) {
//                if (vendor.vendorid == null || vendor.vendorid.toString().trim().length() <= 0) {
//                    errors['vendor'] = 'Vendor must be selected.'
//                }
//            } else {
//                errors['vendor'] = 'Unsupported vendor format.'
//            }
//        }


        return errors
    }




    // Validates the fields of the object and attaches an error message to the model if there are errors
    void validateAndAttachErrorsForCreate(Object object, Model model) {
        Map<String, String> errors = validateFieldsForCreate(object)

        // If there are errors, concatenate them into a single message and add to the model
        if (!errors.isEmpty()) {
            String errorMessage = errors.values().join(' | ')
            modelMessageUtil.addMessage(model, MessageConstants.ERROR_MSG, errorMessage)
        }
    }


    Map<String, String> validateFieldsForCreate(Object object) {
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

        // added fields below here for productVO
        if (object.hasProperty('quantity')) {
            if (object.quantity == null || object.quantity < 0 || object.quantity > 999999) {
                errors['quantity'] = 'Quantity must be an integer between 0 and 999999.'
            }
        }
//
//        if (object.hasProperty('quantityremaining')) {
//            if (object.quantityremaining == null || object.quantityremaining < 0 || object.quantityremaining > 999999) {
//                errors['quantityremaining'] = 'Quantity Remaining must be an integer between 0 and 999999.'
//            }
//        }

        if (object.hasProperty('price')) {
            if (object.price == null || object.price < 0 || object.price >= 999999) {
                errors['price'] = 'Price must be a number between 0 and 999999.'
            }
        }

        if (object.hasProperty('cost')) {
            if (object.cost == null || object.cost < 0 || object.cost >= 999999) {
                errors['cost'] = 'Cost must be a number between 0 and 999999.'
            }
        }

        /* Ensure updateTimeStamp and createTimeStamp are not null */
//        if (object.hasProperty('updateTimeStamp')) {
//            if (object.updateTimeStamp == null) {
//                errors['updateTimeStamp'] = 'Update TimeStamp cannot be empty.'
//            }
//        }
//
//        if (object.hasProperty('createTimeStamp')) {
//            if (object.createTimeStamp == null) {
//                errors['createTimeStamp'] = 'Create TimeStamp cannot be empty.'
//            }
//        }

        /* Ensure productnumber is an integer and not null */
//        if (object.hasProperty('productnumber')) {
//            if (object.productnumber == null) {
//                errors['productnumber'] = 'Product Number cannot be empty.'
//            } else if (!(object.productnumber instanceof Integer)) {
//                errors['productnumber'] = 'Product Number must be an integer.'
//            }
//        }

        /* Ensure producttypeid is not null */
        if (object.hasProperty('producttypeid')) {
            if (object.producttypeid == null) {
                errors['producttypeid'] = 'Product Type ID cannot be null.'
            }
        }

        /* Ensure barcode is exactly 12 digits, an integer, and not null */
//        if (object.hasProperty('barcode')) {
//            if (object.barcode == null || object.barcode.trim().isEmpty()) {
//                errors['barcode'] = 'Barcode cannot be empty.'
//            } else if (!object.barcode.matches("\\d{12}")) {
//                errors['barcode'] = 'Barcode must be exactly 12 digits long and numeric.'
//            }
//
//        }

        /* Ensure vendor is a String and has at least 2 characters */
        if (object.hasProperty('vendor')) {
            if (object.vendor == null || object.vendor.trim().length() <= 1) {
                errors['vendor'] = 'Vendor must be a valid string with at least 2 characters.'
            }
        }

        /* Ensure batch_type_id is an Integer and greater than 0 */
        if (object.hasProperty('batch_type_id')) {
            if (object.batch_type_id == null) {
                errors['batch_type_id'] = 'Batch Type ID cannot be empty.'
            } else if (!(object.batch_type_id instanceof Integer)) {
                errors['batch_type_id'] = 'Batch Type ID must be an integer.'
            } else if (object.batch_type_id <= 0) {
                errors['batch_type_id'] = 'Batch Type ID must be greater than 0.'
            }
        }

        return errors
    }

}