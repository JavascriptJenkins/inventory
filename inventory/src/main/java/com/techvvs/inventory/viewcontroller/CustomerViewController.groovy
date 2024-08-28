package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.PaymentService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.CustomerHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import java.security.SecureRandom

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
        if(model.getAttribute("errorMessage") == null){

            // create the customer
            customerVO = customerHelper.createCustomer(customerVO)
            model.addAttribute("successMessage", "Customer created successfully!")
        }

        model.addAttribute("customer", customerVO)
        customerHelper.addPaginatedData(model, page)
        techvvsAuthService.checkuserauth(model)

        return "customer/customer.html";
    }





}
