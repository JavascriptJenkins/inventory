package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.AttributeRepo
import com.techvvs.inventory.model.AttributeVO
import com.techvvs.inventory.service.attribute.constants.AttributeNameEnum
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.attribute.AttributeService
import com.techvvs.inventory.service.rewards.constants.RewardRegion
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@RequestMapping("/attribute/admin")
@Controller
public class AttributeAdminViewController {
    

    @Autowired
    RewardConfigHelper rewardConfigHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    AttributeService attributeService

    @Autowired
    BatchRepo batchRepo

    @Autowired
    AttributeRepo attributeRepo

    // log expenses to a batch
    @GetMapping
    String viewNewForm(
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("attributeid") Optional<Integer> attributeid

    ){

        techvvsAuthService.checkuserauth(model)

        // attach a blank object to the model
        if(attributeid.isPresent()){
            attributeService.getAttribute(attributeid.get(), model)
        } else {
            attributeService.loadBlankAttribute(model)
        }

        attributeService.addPaginatedData(model, page, size)

        // load the values for dropdowns here
        bindStaticValues(model)

        return "attribute/admin.html";
    }

    void bindStaticValues(Model model) {
        model.addAttribute("attributenames", AttributeNameEnum.values());
//        model.addAttribute("paymentmethods", PaymentMethod.values());
//        model.addAttribute("expensetypes", ExpenseType.values());
//        model.addAttribute("batches", batchRepo.findAll())
//        model.addAttribute("attributes", attributeRepo.findAll())
    }


    @PostMapping("/create")
    String createAttribute(
            @ModelAttribute( "attribute" ) AttributeVO attributeVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        techvvsAuthService.checkuserauth(model)

        attributeVO = attributeService.validateAttribute(attributeVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // create the customer
            attributeVO = attributeService.createAttribute(attributeVO)
            model.addAttribute("successMessage", "Attribute created successfully!")
        }

        model.addAttribute("attribute", attributeVO)
        attributeService.addPaginatedData(model, page, size)
        // load the values for dropdowns here
        bindStaticValues(model)

        return "attribute/admin.html";
    }

    @PostMapping("/edit")
    String editAttribute(
            @ModelAttribute( "attribute" ) AttributeVO attributeVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        attributeService.updateAttribute(attributeVO, model)
        attributeService.addPaginatedData(model, page, size)
        // load the values for dropdowns here
        bindStaticValues(model)
        return "attribute/admin.html"
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
        // load the values for dropdowns here
        bindStaticValues(model)
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
        // load the values for dropdowns here
        bindStaticValues(model)
        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }
}
