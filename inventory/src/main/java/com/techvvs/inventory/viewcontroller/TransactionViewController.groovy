package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
import com.techvvs.inventory.viewcontroller.helper.TransactionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/transaction")
@Controller
public class TransactionViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    TransactionHelper transactionHelper

    @Autowired
    PaymentHelper paymentHelper

    @Autowired
    TransactionService transactionService

    @Autowired
    FilePagingService filePagingService

    @Autowired
    PrinterService printerService
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @GetMapping
    String reviewtransaction(
            @RequestParam(
                    "transactionid" ) String transactionid,
            
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
                     ){

        


        String name = ""
        TransactionVO transactionVO = new TransactionVO()


        transactionVO = transactionService.getExistingTransaction(Integer.valueOf(transactionid))

        // this will set the display quantities
        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)


        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging




        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        checkoutHelper.getAllCustomers(model)


        return "transaction/transactionreview.html";


    }

    @GetMapping("/return")
    String viewNewForm(
            Model model,
            
            @RequestParam("transactionid") String transactionid
    ){

        transactionid = transactionid == null ? "0" : String.valueOf(transactionid)

        // attach the paymentVO to the model
        TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)
        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        model.addAttribute("customer", transactionVO.customervo)


        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        model.addAttribute("transaction", transactionVO);

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transactionid", transactionid);
        return "transaction/return.html";
    }


    @PostMapping("/return")
    String postReturn(
            Model model,
            
            @RequestParam("transactionid") String transactionid,
            @RequestParam("barcode") String barcode
    ){

        transactionid = transactionid == null ? "0" : String.valueOf(transactionid)

        // attach the paymentVO to the model
        TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)

        transactionVO = transactionHelper.deleteProductFromTransaction(transactionVO, barcode)

        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        printerService.printInvoice(transactionVO, false, true) // print another invoice showing return...


        model.addAttribute("customer", transactionVO.customervo)


        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transactionid", transactionid);
        model.addAttribute("transaction", transactionVO);
        model.addAttribute("successMessage", "Deleted the item from transaction successfully with barcode: "+barcode);

        return "transaction/return.html";
    }




    //get the pending transactions
    @GetMapping("/listall")
    String listall(
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        

        // bind the page of transactions
        transactionHelper.findAllTransactions(model, page, size)
        // fetch all customers from database and bind them to model
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        return "transaction/alltransactions.html";
    }





}
