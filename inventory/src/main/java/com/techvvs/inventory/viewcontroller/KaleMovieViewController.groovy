package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.vendor.VendorService
import com.techvvs.inventory.viewcontroller.helper.ConferenceHelper
import com.techvvs.inventory.viewcontroller.helper.CustomerHelper
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@RequestMapping("/kalemovie")
@Controller
public class KaleMovieViewController {
    

    @Autowired
    RewardConfigHelper rewardConfigHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    VendorService vendorService

    @Autowired
    BatchRepo batchRepo

    @Autowired
    VendorRepo vendorRepo

    @Autowired
    CustomerHelper customerHelper

    @Autowired
    ConferenceHelper conferenceHelper

    // log expenses to a batch
    @GetMapping
    String viewNewForm(
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("vendorid") Optional<Integer> vendorid

    ){

        techvvsAuthService.checkuserauth(model)

        // just displaying a plain page here for people to send their contact info
        // attach a blank customer object to the model
        customerHelper.loadBlankCustomer(model)



        return "conference/contactinfo_kale_movie.html";
    }

//    void loadBlankCustomer(Model model) {
//        model.addAttribute("customer", new CustomerVO(customerid: 0))
//    }


    void bindStaticValues(Model model) {
//        model.addAttribute("paymentmethods", PaymentMethod.values());
//        model.addAttribute("expensetypes", ExpenseType.values());
//        model.addAttribute("batches", batchRepo.findAll())
//        model.addAttribute("vendors", vendorRepo.findAll())
    }


    @PostMapping("/create")
    String createVendor(
            @ModelAttribute( "customer" ) CustomerVO customerVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        techvvsAuthService.checkuserauth(model)

// skip validation it is a promo page unlikely to be sql injected here.... also doing frontend validation
//        customerVO = customerHelper.validateCustomer(customerVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // send a text message to my phone with the info and also an email
            boolean success = conferenceHelper.sendCustomerInfoToMyPhoneAndEmailBottleneck(customerVO)


            model.addAttribute("successMessage", "Thank you "+customerVO.name+"!")
        }

        model.addAttribute("customer", customerVO)

        return "conference/contactinfo_kale_movie.html";
    }

    @PostMapping("/edit")
    String editVendor(
            @ModelAttribute( "vendor" ) VendorVO vendorVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        vendorService.updateVendor(vendorVO, model)
        vendorService.addPaginatedData(model, page, size)
        return "vendor/admin.html"
    }

    @GetMapping("/get")
    String getRewardConfig(
            @RequestParam("customerid") Integer customerid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        rewardConfigHelper.getRewardConfig(customerid, model)
        rewardConfigHelper.addPaginatedData(model, page, size)
        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }

    @PostMapping("/delete")
    String deleteRewardConfig(
            @RequestParam("customerid") Integer customerid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        rewardConfigHelper.deleteRewardConfig(customerid, model)
        rewardConfigHelper.addPaginatedData(model, page, size)
        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }
}
