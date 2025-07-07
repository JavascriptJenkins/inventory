package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.MetrcLicenseRepo
import com.techvvs.inventory.model.metrc.MetrcLicenseVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.expense.constants.ExpenseType
import com.techvvs.inventory.service.metrc.constants.LicenseType
import com.techvvs.inventory.service.metrc.license.MetrcLicenseService
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@RequestMapping("/metrc/metrclicense")
@Controller
public class MetrcLicensesViewController {
    

    @Autowired
    RewardConfigHelper rewardConfigHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    MetrcLicenseService metrcLicenseService

    @Autowired
    BatchRepo batchRepo

    @Autowired
    MetrcLicenseRepo metrclicenseRepo

    // todo: enforce admin rights here
    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("metrclicenseid") Optional<Integer> metrclicenseid

    ){

        techvvsAuthService.checkuserauth(model)

        // attach a blank object to the model
        if(metrclicenseid.isPresent()){
            metrcLicenseService.getMetrcLicense(metrclicenseid.get(), model)
        } else {
            metrcLicenseService.loadBlankMetrcLicense(model)
        }

        metrcLicenseService.addPaginatedData(model, page, size)

        // load the values for dropdowns here
        bindStaticValues(model)

        return "metrclicense/admin.html";
    }

    void bindStaticValues(Model model) {
        model.addAttribute("licensetypes", LicenseType.values());

//        model.addAttribute("paymentmethods", PaymentMethod.values());
//        model.addAttribute("expensetypes", ExpenseType.values());
//        model.addAttribute("batches", batchRepo.findAll())
//        model.addAttribute("vendors", metrclicenseRepo.findAll())
    }


    @PostMapping("/create")
    String createMetrcLicense(
            @ModelAttribute( "metrclicense" ) MetrcLicenseVO metrclicenseVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        techvvsAuthService.checkuserauth(model)

        metrclicenseVO = metrcLicenseService.validateMetrcLicense(metrclicenseVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // create the customer
            metrclicenseVO = metrcLicenseService.createMetrcLicense(metrclicenseVO)
            model.addAttribute("successMessage", "MetrcLicense created successfully!")
        }

        model.addAttribute("metrclicense", metrclicenseVO)
        metrcLicenseService.addPaginatedData(model, page, size)

        return "metrclicense/admin.html";
    }

    @PostMapping("/edit")
    String editMetrcLicense(
            @ModelAttribute( "metrclicense" ) MetrcLicenseVO metrclicenseVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        metrcLicenseService.updateMetrcLicense(metrclicenseVO, model)
        metrcLicenseService.addPaginatedData(model, page, size)
        return "metrclicense/admin.html"
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
