package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ConferenceHelper {

    @Autowired
    TransactionHelper transactionHelper

    boolean sendCustomerInfoToMyPhoneAndEmail(CustomerVO customerVO){

        transactionHelper.sendTextMessageWithContactInfo(customerVO)
        transactionHelper.sendEmailWithCustomerInfo(customerVO)


        return true
    }



}
