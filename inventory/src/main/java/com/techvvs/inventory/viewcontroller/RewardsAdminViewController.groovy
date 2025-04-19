package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.rewards.constants.RewardRegion
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/rewards/admin")
@Controller
public class RewardsAdminViewController {
    

    @Autowired
    RewardConfigHelper rewardConfigHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("rewardconfigid") Optional<Integer> rewardconfigid

    ){
        // attach a blank customer object to the model

        if(rewardconfigid.isPresent()){
            rewardConfigHelper.getRewardConfig(rewardconfigid.get(), model)
        } else {
            rewardConfigHelper.loadBlankRewardConfig(model)
        }

        rewardConfigHelper.addPaginatedData(model, page, size)

        techvvsAuthService.checkuserauth(model)

        model.addAttribute("regions", RewardRegion.values());

        return "rewards/admin.html";
    }


    @PostMapping("/create")
    String createcustomer(
            @ModelAttribute( "customer" ) RewardConfigVO customerVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        customerVO = rewardConfigHelper.validateRewardConfig(customerVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // create the customer
            customerVO = rewardConfigHelper.createRewardConfig(customerVO)
            model.addAttribute("successMessage", "RewardConfig created successfully!")
        }

        model.addAttribute("customer", customerVO)
        rewardConfigHelper.addPaginatedData(model, page, size)
        techvvsAuthService.checkuserauth(model)

        return "customer/customer.html";
    }

    @PostMapping("/edit")
    String editRewardConfig(
            @ModelAttribute( "customer" ) RewardConfigVO rewardConfigVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        rewardConfigHelper.updateRewardConfig(rewardConfigVO, model)
        rewardConfigHelper.addPaginatedData(model, page, size)
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("regions", RewardRegion.values());
        return "rewards/admin.html"
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
