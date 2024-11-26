package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.viewcontroller.helper.CustomerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/customer")
@Controller
public class CustomerViewController {
    

    @Autowired
    CustomerHelper customerHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){
        // attach a blank customer object to the model
        customerHelper.loadBlankCustomer(model)

        customerHelper.addPaginatedData(model, page)

        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html";
    }


    @PostMapping("/create")
    String createcustomer(
            @ModelAttribute( "customer" ) CustomerVO customerVO,
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        customerVO = customerHelper.validateCustomer(customerVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // create the customer
            customerVO = customerHelper.createCustomer(customerVO)
            model.addAttribute("successMessage", "Customer created successfully!")
        }

        model.addAttribute("customer", customerVO)
        customerHelper.addPaginatedData(model, page)
        techvvsAuthService.checkuserauth(model)

        return "customer/customer.html";
    }

    @PostMapping("/edit")
    String editCustomer(
            @ModelAttribute( "customer" ) CustomerVO customerVO,
            Model model,
            @RequestParam("page") Optional<Integer> page
    ) {
        customerHelper.updateCustomer(customerVO, model)
        customerHelper.addPaginatedData(model, page)
        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }

    @GetMapping("/get")
    String getCustomer(
            @RequestParam("customerid") Integer customerid,
            Model model,
            @RequestParam("page") Optional<Integer> page
    ) {
        customerHelper.getCustomer(customerid, model)
        customerHelper.addPaginatedData(model, page)
        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }

    @PostMapping("/delete")
    String deleteCustomer(
            @RequestParam("customerid") Integer customerid,
            Model model,
            @RequestParam("page") Optional<Integer> page
    ) {
        customerHelper.deleteCustomer(customerid, model)
        customerHelper.addPaginatedData(model, page)
        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }
}
