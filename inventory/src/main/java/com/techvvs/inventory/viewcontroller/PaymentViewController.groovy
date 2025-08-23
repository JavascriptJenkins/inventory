package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.PaymentService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.persistence.TransactionRequiredException
import java.nio.charset.StandardCharsets


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

    @Autowired
    TransactionService transactionService

    @Autowired
    CartRepo cartRepo

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    TransactionRepo transactionRepo


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



        double paid = transactionVO.getPaid() != null ? transactionVO.getPaid() : 0.0;
        double remaining = Math.max(transactionVO.getTotalwithtax() - paid, 0.0);
        model.addAttribute("amountRemaining", remaining);


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

            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO, model)
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



    // todo: make sure security is enforced here....
    //default home mapping
    @GetMapping("/landingpage")
    String viewPaymentLandingPage(
            Model model,
            @RequestParam("cartid") Optional<String> cartid
    ){

        // bind the CartVO to preparer for paypal checkout
        CartVO cartVO
        if(cartid.isPresent()) {
            cartVO = cartRepo.findById(Integer.valueOf(cartid.get())).get()
            model.addAttribute("cart", cartVO)
            model.addAttribute("customer", cartVO.customer)
            model.addAttribute("cartid", cartVO.cartid)
        } else {
            cartVO = new CartVO(cartid: 0)
            model.addAttribute("cart", cartVO)
            model.addAttribute("customer", new CustomerVO(customerid: 0))
            model.addAttribute("cartid", cartVO.cartid)
        }

        // fetch all customers from database and bind them to model
        techvvsAuthService.checkuserauth(model)
        return "payment/payment_landing.html";
    }


    @GetMapping("/paypal/cancel")
    String viewCancelFromPaypal(
            Model model,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken
    ){


        List<String> authorities = jwtTokenProvider.extractAuthorities(
                shoppingtoken.get().contains(".") ? shoppingtoken.get() : techvvsAuthService.decodeShoppingToken(shoppingtoken))
        if(jwtTokenProvider.hasRole(authorities, String.valueOf(Role.ROLE_PAYPAL_ENABLED))) {


            // bind the CartVO to preparer for paypal checkout
            CartVO cartVO
            if (cartid.isPresent()) {
                cartVO = cartRepo.findById(Integer.valueOf(cartid.get())).get()
                model.addAttribute("cart", cartVO)
                model.addAttribute("customer", cartVO.customer)
                model.addAttribute("cartid", cartVO.cartid)
            } else {
                cartVO = new CartVO(cartid: 0)
                model.addAttribute("cart", cartVO)
                model.addAttribute("customer", new CustomerVO(customerid: 0))
                model.addAttribute("cartid", cartVO.cartid)
            }
        } else{
            model.addAttribute("errorMessage", "You do not have permission to view this page")
        }

        return "payment/payment_landing.html";
    }

    @GetMapping("/paypal/return")
    String viewReturnFromPaypal(
            Model model,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken
    ){
        List<String> authorities = jwtTokenProvider.extractAuthorities(
                shoppingtoken.get().contains(".") ? shoppingtoken.get() : techvvsAuthService.decodeShoppingToken(shoppingtoken))
        if(jwtTokenProvider.hasRole(authorities, String.valueOf(Role.ROLE_PAYPAL_ENABLED))) {

            // bind the CartVO to preparer for paypal checkout
            CartVO cartVO
            if (cartid.isPresent()) {
                cartVO = cartRepo.findById(Integer.valueOf(cartid.get())).get()
                model.addAttribute("cart", cartVO)
                model.addAttribute("customer", cartVO.customer)
                model.addAttribute("cartid", cartVO.cartid)
            } else {
                cartVO = new CartVO(cartid: 0)
                model.addAttribute("cart", cartVO)
                model.addAttribute("customer", new CustomerVO(customerid: 0))
                model.addAttribute("cartid", cartVO.cartid)
            }
        } else{
            model.addAttribute("errorMessage", "You do not have permission to view this page")
        }
        return "payment/payment_landing.html";
    }


    @GetMapping("/paypal/thank-you")
    String viewCaptureFromPaypal(
            Model model,
            @RequestParam("orderId") Optional<String> orderId,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken
    ){
        List<String> authorities = jwtTokenProvider.extractAuthorities(
                shoppingtoken.get().contains(".") ? shoppingtoken.get() : techvvsAuthService.decodeShoppingToken(shoppingtoken))
        if(jwtTokenProvider.hasRole(authorities, String.valueOf(Role.ROLE_PAYPAL_ENABLED))) {

            // bind the CartVO to preparer for paypal checkout
            TransactionVO transactionVO
            if (orderId.isPresent()) {

                transactionVO = transactionRepo.findByPaypalOrderId(orderId.get()).get()
                transactionVO.updateProductDisplayQuantities()
                model.addAttribute("transaction", transactionVO)
                model.addAttribute("customer", transactionVO.customervo)
                String encoded = Base64.getEncoder().encodeToString(shoppingtoken.get().getBytes(StandardCharsets.UTF_8));
                model.addAttribute("shoppingtoken", encoded)
            } else {
                model.addAttribute("transaction", null)
                model.addAttribute("customer", null)
                model.addAttribute("errorMessage", "Transaction not found.  This is bad and should not happen.  Please contact support.")
                String encoded = Base64.getEncoder().encodeToString(shoppingtoken.get().getBytes(StandardCharsets.UTF_8));
                model.addAttribute("shoppingtoken", encoded)
            }
        } else{
            model.addAttribute("errorMessage", "You do not have permission to view this page")
        }
        return "payment/payment_success.html";
    }



}
