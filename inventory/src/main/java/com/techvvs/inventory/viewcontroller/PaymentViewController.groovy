package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.PaymentService
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/payment")
@Controller
public class PaymentViewController {

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    PrinterService printerService

    @Autowired
    PaymentHelper paymentHelper

    @Autowired
    PaymentService paymentService

    @Autowired
    TechvvsAuthService techvvsAuthService


    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "payment" ) PaymentVO paymentVO,
            Model model,
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

        



        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transactionid", transactionid);
        return "payment/payment.html";
    }


    @PostMapping("/submitpayment")
    String submitpayment(
            @ModelAttribute( "payment" ) PaymentVO paymentVO,
            @RequestParam( "transactionid" ) String transactionid,
            @RequestParam( "customerid" ) String customerid,
            Model model
    ){

        


        paymentVO = paymentHelper.validatePaymentVO(paymentVO, model, Integer.valueOf(transactionid))

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            TransactionVO transactionVO = paymentService.submitPaymentForTransaction(Integer.valueOf(transactionid),Integer.valueOf(customerid),paymentVO)
            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)
            printerService.printInvoice(transactionVO, false, true)


            model.addAttribute("transaction", transactionVO)
            model.addAttribute("transactionid", transactionVO.transactionid)
            model.addAttribute("customer", transactionVO.customervo)
            transactionVO.getMostRecentPayment().amountpaid = 0
            model.addAttribute("payment", transactionVO.getMostRecentPayment())

            model.addAttribute("successMessage", "Payment submitted successfully!")


        } else {
             CustomerVO customerVO = paymentHelper.loadCustomer(customerid, model)
             TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)


            model.addAttribute("customer", customerVO)
            model.addAttribute("transaction", transactionVO)
            paymentVO.setAmountpaid(0)
            model.addAttribute("payment", paymentVO)
        }


        techvvsAuthService.checkuserauth(model)
        // fetch all customers from database and bind them to model

        return "payment/payment.html";
    }





}
