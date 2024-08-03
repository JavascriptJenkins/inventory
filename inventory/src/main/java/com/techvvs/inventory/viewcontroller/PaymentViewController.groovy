package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.PaymentService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import java.security.SecureRandom

@RequestMapping("/payment")
@Controller
public class PaymentViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    BatchDao batchDao;


    @Autowired
    BatchTypeRepo batchTypeRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;


    @Autowired
    ProductRepo productRepo;


    @Autowired
    ValidateBatch validateBatch;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    MenuHelper menuHelper

    @Autowired
    CartService cartService

    @Autowired
    TransactionService transactionService

    @Autowired
    PrinterService printerService

    @Autowired
    PaymentHelper paymentHelper

    @Autowired
    PaymentService paymentService


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "payment" ) PaymentVO paymentVO,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("transactionid") String transactionid
    ){

        transactionid = transactionid == null ? "0" : String.valueOf(transactionid)
        String paymentid = paymentVO.paymentid == null ? "0" : String.valueOf(paymentVO.paymentid)

        // attach the paymentVO to the model
        paymentVO = paymentHelper.loadPayment(paymentid, model)
        TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)
        model.addAttribute("customer", transactionVO.customervo)



        // todo: add a button on the ui to pull the latest transaction for customer (so if someone clicks off page
        //  you can come back and finish the transaction)

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);



        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("transactionid", transactionid);
//        model.addAttribute("transaction", transactionVO);
        return "payment/payment.html";
    }


    @PostMapping("/submitpayment")
    String submitpayment(
            @ModelAttribute( "payment" ) PaymentVO paymentVO,
            @RequestParam( "transactionid" ) String transactionid,
            @RequestParam( "customerid" ) String customerid,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter
    ){

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);


        paymentVO = paymentHelper.validatePaymentVO(paymentVO, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            TransactionVO transactionVO = paymentService.submitPaymentForTransaction(Integer.valueOf(transactionid),Integer.valueOf(customerid),paymentVO)
            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)
            printerService.printInvoice(transactionVO, false, true)


            model.addAttribute("transaction", transactionVO)
            model.addAttribute("transactionid", transactionVO.transactionid)
            model.addAttribute("customer", transactionVO.customervo)
            model.addAttribute("payment", transactionVO.getMostRecentPayment())

            model.addAttribute("successMessage", "Payment submitted successfully!")


        } else {
             CustomerVO customerVO = paymentHelper.loadCustomer(customerid, model)
             TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)


            model.addAttribute("customer", customerVO)
            model.addAttribute("transaction", transactionVO)
            model.addAttribute("payment", paymentVO)
        }


        model.addAttribute("customJwtParameter", customJwtParameter);
        // fetch all customers from database and bind them to model

        return "payment/payment.html";
    }





}
