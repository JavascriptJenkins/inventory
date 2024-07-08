package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.model.CustomerVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

// helper class to make sure actual checkout controller code stays clean n tidy
@Component
class CheckoutHelper {

    @Autowired
    CustomerRepo customerRepo


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }






}
