package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.ExpenseVO
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.model.nonpersist.graphs.RevenueDataPoint
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.expense.ExpenseService
import com.techvvs.inventory.service.expense.constants.ExpenseType
import com.techvvs.inventory.service.expense.constants.PaymentMethod
import com.techvvs.inventory.service.rewards.constants.RewardRegion
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import com.techvvs.inventory.viewcontroller.helper.TransactionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors


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

    @Autowired
    TransactionHelper transactionHelper

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    ProductRepo productRepo

    @Autowired
    TransactionRepo transactionRepo

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



    @GetMapping('/sales')
    String viewSales(
            Model model,

            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("batchid") Optional<Integer> batchinscope,
            @RequestParam("customerid") Optional<Integer> customerinscope,
            @RequestParam("productid") Optional<Integer> productinscope,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startdate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> enddate


    ){

        Integer customerid = (customerinscope.present && customerinscope.get() > 0) ? customerinscope.get() : null
        Integer productid = (productinscope.present && productinscope.get() > 0) ? productinscope.get() : null
        Integer batchid = (batchinscope.present && batchinscope.get() > 0) ? batchinscope.get() : null


        List<Object[]> results = transactionRepo.findDailyRevenueByFilters(
                startdate.orElse(LocalDateTime.now().minusMonths(1)),
                enddate.orElse(LocalDateTime.now()),
                customerid,
                productid,
                batchid
        );

        Map<LocalDate, Double> revenueMap = results.collectEntries { row ->
            def date = ((java.sql.Date) row[0]).toLocalDate()
            def amount = (Double) row[1]
            [(date): amount]
        }


        // Ensure full date range is represented
        LocalDate start = startdate.orElse(LocalDateTime.now().minusMonths(1)).toLocalDate()
        LocalDate end = enddate.orElse(LocalDateTime.now()).toLocalDate()

        List<RevenueDataPoint> response = []
        LocalDate current = start
        while (!current.isAfter(end)) {
            response << new RevenueDataPoint(current, revenueMap.getOrDefault(current, 0.0))
            current = current.plusDays(1)
        }


        techvvsAuthService.checkuserauth(model)

        bindStaticValuesForSalesGraphPage(model, batchinscope, productinscope, customerinscope, startdate, enddate)
        model.addAttribute("revenuedata", response)

        return "accounting/sales.html";
    }

    void bindStaticValuesForSalesGraphPage(Model model,
                                           Optional<Integer> batchinscope,
                                           Optional<Integer> productinscope,
                                           Optional<Integer> customerinscope,
                                           Optional<LocalDateTime> startdate,
                                           Optional<LocalDateTime> enddate
    ) {

        model.addAttribute("startdate", startdate.orElse(LocalDateTime.now().minusMonths(1))); // or null/0 if not filtering
        model.addAttribute("enddate", enddate.orElse(LocalDateTime.now())); // or null/0 if not filtering


        checkoutHelper.getAllCustomers(model)
        model.addAttribute("customerinscope", customerinscope.orElse(0)); // or null/0 if not filtering

        model.addAttribute("batches", batchRepo.findAllByOrderByCreateTimeStampDescNameAsc());
        model.addAttribute("batchinscope", batchinscope.orElse(0)); // or null/0 if not filtering

        model.addAttribute("products", productRepo.findAllByOrderByCreateTimeStampDescNameAsc());
        model.addAttribute("productinscope", productinscope.orElse(0)); // or null/0 if not filtering

    }
}
