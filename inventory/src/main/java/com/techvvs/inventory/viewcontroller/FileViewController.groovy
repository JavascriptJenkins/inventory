package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
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
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom

@RequestMapping("/viewfiles")
@Controller
public class FileViewController {


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
            @ModelAttribute( "batch" ) BatchVO batchVO,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("batchid") String batchid
    ){

//        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+batchnumber+appConstants.BARCODES_MENU_DIR));
//
//        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchid, Paths.get(appConstants.PARENT_LEVEL_DIR+batchnumber+appConstants.BARCODES_MENU_DIR));
//        model.addAttribute("filelist",filelist);



        batchid = batchid == null ? "0" : String.valueOf(batchid)

        // attach the paymentVO to the model
        batchVO = batchControllerHelper.loadBatch(batchid, model)

        // fetch all customers from database and bind them to model
        model.addAttribute("customJwtParameter", customJwtParameter);
        return "files/batchfiles.html";
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
