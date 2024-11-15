package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.DeliveryService
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

    @Autowired
    LocationRepo locationRepo

    @Autowired
    DeliveryService deliveryService

    @Autowired
    ProductTypeRepo productTypeRepo

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

    @GetMapping("/discount")
    String viewNewFormDiscount(
            Model model,
            @RequestParam("transactionid") String transactionid,
            @RequestParam("producttypeid") Optional<String> producttypeid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        transactionid = transactionid == null ? "0" : String.valueOf(transactionid)

        // attach the paymentVO to the model
        TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)

        if(producttypeid.isPresent()){
            // if we have a producttype then filter out all products that are not of that producttype
            transactionVO.product_list = transactionVO.product_list.findAll { product ->
                product?.producttypeid?.producttypeid == Integer.valueOf(producttypeid.get())
            }
        }
        model.addAttribute("producttypeid", producttypeid.orElse("0"));
        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        model.addAttribute("customer", transactionVO.customervo)


        // fetch all producttypes from database and bind them to model
        model.addAttribute("producttypes", productTypeRepo.findAll()); // for the filter dropdown
        model.addAttribute("transaction", transactionVO);

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transactionid", transactionid);
        return "transaction/discount.html";
    }


    @PostMapping("/discount")
    String postDiscount(
            Model model,
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            @RequestParam("transactionid") String transactionid,
            @RequestParam("barcode") String barcode,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

      //  transactionid = transactionid == null ? "0" : String.valueOf(transactionid)

        // attach the paymentVO to the model
     //   TransactionVO transactionVO = paymentHelper.loadTransaction(transactionid, model)

     //   transactionVO = transactionHelper.deleteProductFromTransaction(transactionVO, barcode)




        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)
        printerService.printInvoice(transactionVO, false, true) // print another invoice showing discount...
        model.addAttribute("customer", transactionVO.customervo)


        model.addAttribute("producttypes", productTypeRepo.findAll()); // for the filter dropdown
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transactionid", transactionid);
        model.addAttribute("transaction", transactionVO);
        model.addAttribute("successMessage", "Deleted the item from transaction successfully with barcode: "+barcode);

        return "transaction/discount.html";
    }




    //get the pending transactions
    @GetMapping("/listall")
    String listall(
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("customerid") Optional<Integer> customerid
    ){

        

        // bind the page of transactions
        transactionHelper.findAllTransactions(model, page, size, customerid)

        //transactionHelper.applyCustomerFilter(transactionVO, model)

        // fetch all customers from database and bind them to model
        techvvsAuthService.checkuserauth(model)
        checkoutHelper.getAllCustomers(model)
        model.addAttribute("transaction", transactionVO);
        return "transaction/alltransactions.html";
    }



    //get the pending transactions with unpaid products in them from a certain batch
    @GetMapping("/unpaid")
    String unpaid(
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("batchid") Optional<Integer> batchid
    ){



        // bind the page of transactions
        transactionHelper.findAllUnpaidProductsInTransactionsByBatchId(model, page, size, batchid)

        //transactionHelper.applyCustomerFilter(transactionVO, model)

        // fetch all customers from database and bind them to model
        techvvsAuthService.checkuserauth(model)
        checkoutHelper.getAllBatches(model) // bind the batchlist
        model.addAttribute("transaction", transactionVO);
        return "transaction/unpaidproducts.html";
    }


    //get the pending transactions with unpaid products in them from a certain batch
    @GetMapping("/unpaidtransactions")
    String unpaidtransactions(
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("batchid") Optional<Integer> batchid,
            @RequestParam("product_id") Optional<Integer> productid
    ){



        // bind the page of transactions
        transactionHelper.findAllUnpaidTransactionsByBatchIdAndProduct_id(model, page, size, batchid, productid)

        //transactionHelper.applyCustomerFilter(transactionVO, model)

        // fetch all customers from database and bind them to model
        techvvsAuthService.checkuserauth(model)
        checkoutHelper.getAllBatches(model) // bind the batchlist
        model.addAttribute("transaction", transactionVO);
        return "transaction/unpaidtransactions.html";
    }



    // This handles displaying the delivery popup data
    @GetMapping("/delivery")
    String delivery(
            @RequestParam("transactionid" ) String transactionid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("popup") Optional<Integer> popup
    ){
        // bind all page data under the pop (waste of resources but its a nice UI..... )
        bindTransactionReviewDataUnderPopUp(transactionid, model, page, size)


        TransactionVO transactionVO = (TransactionVO) model.getAttribute("transaction")

//        if(transactionVO.delivery != null){
//            model.addAttribute("delivery", transactionVO.delivery)
//        }

        model.addAttribute("locationlist", locationRepo.findAll())

        model.addAttribute("popup", popup.orElse(0)) // 1 shows popup, 0 doesnt


        techvvsAuthService.checkuserauth(model)
        return "transaction/transactionreview.html";
    }


    // This handles displaying the delivery popup data
    @PostMapping("/createdelivery")
    String createdelivery(
            @RequestParam("transactionid" ) String transactionid,
            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("popup") Optional<Integer> popup
    ){

        // here we create the delivery
        deliveryService.createNewDeliveryFromTransaction(transactionVO)


        // bind all page data under the pop (waste of resources but its a nice UI..... )
        bindTransactionReviewDataUnderPopUp(transactionid, model, page, size)
        transactionVO = (TransactionVO) model.getAttribute("transaction")
        model.addAttribute("locationlist", locationRepo.findAll())
        model.addAttribute("popup", popup.orElse(0)) // 1 shows popup, 0 doesnt
        techvvsAuthService.checkuserauth(model)
        return "transaction/transactionreview.html";
    }

    // this binds all the data under the delivery popup so it remains while we do delivery stuff.
    // this is probably not good for performance (loading stuff for no reason.....), but I am going to see how we like it.
    void bindTransactionReviewDataUnderPopUp(String transactionid, Model model, Optional<Integer> page, Optional<Integer> size){
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

        model.addAttribute("transaction", transactionVO);
        checkoutHelper.getAllCustomers(model)
    }

}
