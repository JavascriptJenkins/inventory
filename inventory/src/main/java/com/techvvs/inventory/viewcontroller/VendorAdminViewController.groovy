package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.rewards.constants.RewardRegion
import com.techvvs.inventory.service.vendor.VendorService
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/vendor/admin")
@Controller
public class VendorAdminViewController {
    

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

    // log expenses to a batch
    @GetMapping
    String viewNewForm(
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("vendorid") Optional<Integer> vendorid

    ){

        techvvsAuthService.checkuserauth(model)

        // attach a blank object to the model
        if(vendorid.isPresent()){
            vendorService.getVendor(vendorid.get(), model)
        } else {
            vendorService.loadBlankVendor(model)
        }

        vendorService.addPaginatedData(model, page, size)

        // Add size parameter to model for template
        model.addAttribute("size", size.orElse(100))

        // load the values for dropdowns here
        bindStaticValues(model)

        return "vendor/admin.html";
    }

    void bindStaticValues(Model model) {
//        model.addAttribute("paymentmethods", PaymentMethod.values());
//        model.addAttribute("expensetypes", ExpenseType.values());
//        model.addAttribute("batches", batchRepo.findAll())
//        model.addAttribute("vendors", vendorRepo.findAll())
    }


    @PostMapping("/create")
    String createVendor(
            @ModelAttribute( "vendor" ) VendorVO vendorVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        techvvsAuthService.checkuserauth(model)

        vendorVO = vendorService.validateVendor(vendorVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // create the customer
            vendorVO = vendorService.createVendor(vendorVO)
            model.addAttribute("successMessage", "Vendor created successfully!")
        }

        model.addAttribute("vendor", vendorVO)
        vendorService.addPaginatedData(model, page, size)
        
        // Add size parameter to model for template
        model.addAttribute("size", size.orElse(100))

        return "vendor/admin.html";
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
        
        // Add size parameter to model for template
        model.addAttribute("size", size.orElse(100))
        
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
