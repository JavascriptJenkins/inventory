package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.TransactionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.SecureRandom
import java.time.LocalDateTime

@RequestMapping("/transaction")
@Controller
public class TransactionViewController {


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
    TransactionHelper transactionHelper

    @Autowired
    CartService cartService

    @Autowired
    TransactionService transactionService

    @Autowired
    PrinterService printerService


    SecureRandom secureRandom = new SecureRandom();


    @GetMapping
    String reviewtransaction(
            @RequestParam(
                    "transactionid" ) String transactionid,
            @RequestParam("customJwtParameter") String customJwtParameter,
            Model model
                     ){

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);


        String name = ""
        TransactionVO transactionVO = new TransactionVO()

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            transactionVO = transactionService.getExistingTransaction(Integer.valueOf(transactionid))

            // this will set the display quantities
            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)


            name = transactionVO.cart.customer.name

        }


        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("transaction", transactionVO);
        checkoutHelper.getAllCustomers(model)


        return "transaction/transactionreview.html";


    }



    //get the pending transactions
    @GetMapping("/listall")
    String listall(
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);

        // bind the page of transactions
        transactionHelper.findAllTransactions(model, page, size)
        // fetch all customers from database and bind them to model
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("transaction", transactionVO);
        return "transaction/alltransactions.html";
    }





}
