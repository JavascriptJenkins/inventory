package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.ExpenseVO
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.expense.ExpenseService
import com.techvvs.inventory.service.expense.constants.ExpenseType
import com.techvvs.inventory.service.expense.constants.PaymentMethod
import com.techvvs.inventory.service.rewards.constants.RewardRegion
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/accounting/admin")
@Controller
public class AccountingAdminViewController {
    

    @Autowired
    RewardConfigHelper rewardConfigHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    ExpenseService expenseService

    @Autowired
    BatchRepo batchRepo

    @Autowired
    VendorRepo vendorRepo

    // log expenses to a batch
    @GetMapping('/expenses')
    String viewNewForm(
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("expenseid") Optional<Integer> expenseid

    ){

        techvvsAuthService.checkuserauth(model)

        // attach a blank object to the model
        if(expenseid.isPresent()){
            expenseService.getExpense(expenseid.get(), model)
        } else {
            expenseService.loadBlankExpense(model)
        }

        expenseService.addPaginatedData(model, page, size)

        // load the values for dropdowns here
        bindStaticValues(model)

        return "expense/admin.html";
    }

    void bindStaticValues(Model model) {
        model.addAttribute("paymentmethods", PaymentMethod.values());
        model.addAttribute("expensetypes", ExpenseType.values());
        model.addAttribute("batches", batchRepo.findAll())
        model.addAttribute("vendors", vendorRepo.findAll())
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

    @PostMapping("/expenses/edit")
    String editRewardConfig(
            @ModelAttribute( "expense" ) ExpenseVO expenseVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {


        techvvsAuthService.checkuserauth(model)
        int systemuserid = techvvsAuthService.getSystemIdOfCurrentUser()
        expenseVO.systemuser = systemuserid

        // pull out the user info and set it as userid on the expense object


        expenseService.updateExpense(expenseVO, model)
        expenseService.addPaginatedData(model, page, size)
//
        // load the values for dropdowns here
        bindStaticValues(model)
        return "expense/admin.html"
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
