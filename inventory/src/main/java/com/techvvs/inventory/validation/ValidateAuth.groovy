package com.techvvs.inventory.validation

import com.google.common.base.CharMatcher
import com.techvvs.inventory.model.SystemUserDAO
import org.springframework.stereotype.Component

@Component
class ValidateAuth {


    String validateCreateSystemUser(SystemUserDAO systemUserDAO) {

        if (systemUserDAO.getPhone() != null) {
            String theDigits = CharMatcher.inRange('0' as char, '9' as char).retainFrom(systemUserDAO.getPhone());
            systemUserDAO.setPhone(theDigits);
        }

        if (systemUserDAO.getPhone().length() > 11
                || systemUserDAO.getPhone().length() < 10
                || systemUserDAO.getPhone().contains("-")
                || systemUserDAO.getPhone().contains(".")
        ) {
            return "enter 10 or 11 digit phone number (special characters are ignored).  ex. 18884445555";
        } else {
            systemUserDAO.setPhone(systemUserDAO.getPhone().trim());
            systemUserDAO.setPhone(systemUserDAO.getPhone().replaceAll(" ", ""));
        }

//        if (systemUserDAO.getPhone().length() == 10) {
//            systemUserDAO.setPhone("1" + systemUserDAO.getPhone()); // add usa country code if phone number is 10 digits
//        }

        if (systemUserDAO.getEmail().length() < 6
                || systemUserDAO.getEmail().length() > 200
                || !systemUserDAO.getEmail().contains("@")
        // todo: write method here to make sure there is text between @ and .com in the string

        ) {
            return "email must be between 6-200 characters and contain @ and .com";
        } else {
            systemUserDAO.setEmail(systemUserDAO.getEmail().trim());
            systemUserDAO.setEmail(systemUserDAO.getEmail().replaceAll(" ", ""));
        }


        if (systemUserDAO.getPassword().length() > 200
                || systemUserDAO.getPassword().length() < 8) {
            return "password must be between 8-200 characters";
        }
        return "success";
    }


    String validateLoginInfo(SystemUserDAO systemUserDAO) {

        // note - not validating password in here becasue it's not needed until phonetoken/password page
        if (systemUserDAO.getEmail() == null || systemUserDAO.getEmail().isBlank()) {
            return "email is blank";
        }


        if (systemUserDAO.getEmail().length() < 6
                || systemUserDAO.getEmail().length() > 200
                || !systemUserDAO.getEmail().contains("@")

        ) {
            return "email must be between 6-200 characters and contain @ and .com";
        } else {
            systemUserDAO.setEmail(systemUserDAO.getEmail().trim());
            systemUserDAO.setEmail(systemUserDAO.getEmail().replaceAll(" ", ""));
        }

        // note - not validating password in here becasue it's not needed until phonetoken/password page

//        if( systemUserDAO.getPassword().length() > 200
//                || systemUserDAO.getPassword().length() < 8 ){
//            return "password must be between 8-200 characters";
//        }
        return "success";
    }


    String validateActuallyResetPasswordInfo(SystemUserDAO systemUserDAO) {

        if (systemUserDAO.getEmail() == null || systemUserDAO.getPassword() == null || systemUserDAO.getEmail().isBlank() || systemUserDAO.getPassword().isBlank()) {
            return "either email or password is blank";
        }


        if (systemUserDAO.getEmail().length() < 6
                || systemUserDAO.getEmail().length() > 200
                || !systemUserDAO.getEmail().contains("@")

        ) {
            return "email must be between 6-200 characters and contain @ and .com";
        } else {
            systemUserDAO.setEmail(systemUserDAO.getEmail().trim());
            systemUserDAO.setEmail(systemUserDAO.getEmail().replaceAll(" ", ""));
        }


        if (systemUserDAO.getPassword().length() > 200
                || systemUserDAO.getPassword().length() < 8) {
            return "password must be between 8-200 characters";
        }
        return "success";
    }





}
