package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse
import java.time.LocalDateTime

@RequestMapping("/checkout")
@Controller
public class CheckoutViewController {

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    AppConstants appConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    BatchDao batchDao;
    
    @Autowired
    ValidateBatch validateBatch;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    CartService cartService

    @Autowired
    TransactionService transactionService

    @Autowired
    PrinterService printerService
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "cart" ) CartVO cartVO,
            Model model,
            
            @RequestParam("cartid") String cartid

    ){


        model = checkoutHelper.loadCartForCheckout(cartid, model, cartVO)

        // todo: add a button on the ui to pull the latest transaction for customer (so if someone clicks off page
        //  you can come back and finish the transaction)

        


        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        techvvsAuthService.checkuserauth(model)
        return "checkout/checkout.html";
    }

    //get the pending carts
    @GetMapping("pendingcarts")
    String viewPendingTransactions(
            @ModelAttribute( "cart" ) CartVO cartVO,
            Model model,
            
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        

        // bind the page of transactions
        checkoutHelper.findPendingCarts(model, page, size)
        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("cart", cartVO);
        return "checkout/pendingcarts.html";
    }

    // todo: write in a validation check to make sure you can't add more than is available in the batch
    // first user creates a cart by scanning items.
    // on next page "final checkout", the cart will be transformed into a transaction
    @PostMapping("/scan")
    String scan(@ModelAttribute( "cart" ) CartVO cartVO,
                Model model,
                
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size){

        

        cartVO = cartDeleteService.validateCartVO(cartVO, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one

            cartVO = cartDeleteService.saveCartIfNew(cartVO)

            //  if the transactionVO comes back here without a
            // after transaction is created, search for the product based on barcode

           // cartVO = cartService.searchForProductByBarcode(cartVO, model, page, size)
            cartVO = cartDeleteService.searchForProductByBarcode(cartVO, model, page, size)


        }

        cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)
        

        cartVO.barcode = "" // reset barcode to empty

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("cart", cartVO);
        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)

        return "checkout/checkout.html";
    }

    @PostMapping("/delete")
    String delete(
                Model model,
                
                @RequestParam("cartid") String cartid,
                @RequestParam("barcode") String barcode,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size){

        

        CartVO cartVO = checkoutHelper.getExistingCart(cartid)

        cartVO = cartDeleteService.deleteProductFromCart(cartVO, barcode)

        cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)

        cartVO.barcode = "" // reset barcode to empty

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("cart", cartVO);
        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)

        return "checkout/checkout.html";
    }


    @PostMapping("/reviewcart")
    String reviewcart(@ModelAttribute( "cart" ) CartVO cartVO,
                Model model,
                
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size){

        

        cartVO = checkoutHelper.validateCartReviewVO(cartVO, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one
            cartVO = checkoutHelper.getExistingCart(String.valueOf(cartVO.cartid))
            
            cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)

        }

        cartVO.barcode = "" // reset barcode to empty

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("cart", cartVO);
        model.addAttribute("successMessage", "Review the cart")
        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)

        return "checkout/reviewcart.html";
    }

    // this will process the cart and create transaction records
    @PostMapping("/transaction")
    String transaction(@ModelAttribute( "cart" ) CartVO cartVO,
                      Model model,
                      
                      @RequestParam("page") Optional<Integer> page,
                      @RequestParam("size") Optional<Integer> size){

        

        cartVO = checkoutHelper.validateCartReviewVO(cartVO, model)
        TransactionVO transactionVO = new TransactionVO()


        String name = ""

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            cartVO = cartService.getExistingCart(cartVO)


            name =cartVO.customer.name

            // save a new transaction object in database if we don't have one
            transactionVO = transactionService.processCartGenerateNewTransaction(cartVO)

        }



        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)

        techvvsAuthService.checkuserauth(model)


        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully completed transaction! Thanks "+name+"!")

            // start file paging
            String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
            Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
            filePagingService.bindPageAttributesToModel(model, filePage, page, size);
            // end file paging

            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

            printerService.printInvoice(transactionVO, false, true)

            model.addAttribute("transaction", transactionVO);
            return "transaction/transactionreview.html";

        } else {
            return "checkout/transaction.html";// return same page with errors
        }

    }


    @PostMapping("/printreceipt")
    String printreceipt(@ModelAttribute( "transaction" ) TransactionVO transactionVO,
                       Model model,
                       
                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size){

        


        String name = ""

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            transactionVO = transactionService.getExistingTransaction(transactionVO.transactionid)

            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

            printerService.printReceipt(transactionVO)

            name = transactionVO.cart.customer.name

        }

        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging


        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        checkoutHelper.getAllCustomers(model)

        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully printed receipt! Thanks "+name+"!")

            return "transaction/transactionreview.html";

        } else {
            return "transaction/transactionreview.html";// return same page with errors
        }

    }


    @PostMapping("/printinvoice")
    String printinvoice(@ModelAttribute( "transaction" ) TransactionVO transactionVO,
                        Model model,
                        
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size){

        


        String name = ""

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            transactionVO = transactionService.getExistingTransaction(transactionVO.transactionid)

            transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

            printerService.printInvoice(transactionVO, true, false)

            name = transactionVO.cart.customer.name

        }

        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        checkoutHelper.getAllCustomers(model)

        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully printed invoice! Thanks "+name+"!")

            return "transaction/transactionreview.html";

        } else {
            return "transaction/transactionreview.html";// return same page with errors
        }

    }

    @PostMapping("/textemailinvoice")
    String textemailinvoice(@ModelAttribute( "transaction" ) TransactionVO transactionVO,
                        Model model,
                        
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size){

        // start bind transients
        String transientemail = transactionVO.email
        String transientphonunumber = transactionVO.phonenumber
        String transientaction =  transactionVO.action
        String filename =  transactionVO.filename
        // end bind transients
        String contentsofinvoice = ""

        transactionVO = transactionService.getExistingTransaction(transactionVO.transactionid)

        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        checkoutHelper.bindtransients(transactionVO, transientphonunumber, transientemail, transientaction)

        if("view".equals(transactionVO.action)){
            filename = filename.replaceAll(",", "")
            String dirandfilename = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"+filename
            contentsofinvoice = techvvsFileHelper.readPdfAsBase64String(dirandfilename)

        } else {
            // todo: need to modify this to pass in the actual invoice from the table row clicked, instead of how it is now, which texts/emails the most recent invoice
            printerService.printInvoice(transactionVO, false, false)
        }



        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        model.addAttribute("invoicecontent", contentsofinvoice)
        checkoutHelper.getAllCustomers(model)

        if(model.getAttribute("errorMessage") == null && contentsofinvoice.length() == 0){
            model.addAttribute("successMessage", "Successfully sent invoice! Thanks!")

            return "transaction/transactionreview.html";

        } else if(contentsofinvoice.length() > 0) {
            model.addAttribute("successMessage", "Successfully retrieved invoice for display. ")
            return "transaction/transactionreview.html";// return same page with errors
        } else {
            return "transaction/transactionreview.html";// return same page with errors
        }

    }

    // NOTE: this is not being used right now, but this will indeed print the most recent invoice when clicked from the transaction review page in the table of invoices
    @PostMapping("/printmostrecentinvoice")
    String printmostrecentinvoice(@ModelAttribute( "transaction" ) TransactionVO transactionVO,
                            Model model,
                            
                            @RequestParam("page") Optional<Integer> page,
                            @RequestParam("size") Optional<Integer> size){

        

        // start bind transients
        String transientemail = transactionVO.email
        String transientphonunumber = transactionVO.phonenumber
        String transientaction =  transactionVO.action
        // end bind transients

        transactionVO = transactionService.getExistingTransaction(transactionVO.transactionid)

        transactionVO = checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        checkoutHelper.bindtransients(transactionVO, transientphonunumber, transientemail, transientaction)
        printerService.printInvoice(transactionVO, false, false)


        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        checkoutHelper.getAllCustomers(model)

        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully sent invoice! Thanks!")

            return "transaction/transactionreview.html";

        } else {
            return "transaction/transactionreview.html";// return same page with errors
        }

    }

    @GetMapping("/browseBatch")
    String browseBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size ){

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<BatchVO> pageOfBatch = batchRepo.findAll(pageable);

        int totalPages = pageOfBatch.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfBatch.getTotalPages());
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        model.addAttribute("batchPage", pageOfBatch);
        return "checkout/browsebatch.html";
    }

    @GetMapping("/searchBatch")
    String searchBatch(@ModelAttribute( "batch" ) BatchVO batchVO, Model model){

        

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        model.addAttribute("batchs", new ArrayList<BatchVO>(1));
        return "checkout/searchbatch.html";
    }

    @PostMapping("/searchBatch")
    String searchBatchPost(@ModelAttribute( "batch" ) BatchVO batchVO, Model model){

        

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchVO.getBatchnumber() != null){
            System.out.println("Searching data by getBatchnumber");
            results = batchRepo.findAllByBatchnumber(batchVO.getBatchnumber());
        }
        if(batchVO.getName() != null && results.size() == 0){
            System.out.println("Searching data by getName");
            results = batchRepo.findAllByName(batchVO.getName());
        }
        if(batchVO.getDescription() != null && results.size() == 0){
            System.out.println("Searching data by getDescription");
            results = batchRepo.findAllByDescription(batchVO.getDescription());
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", batchVO);
        model.addAttribute("batchs", results);
        return "checkout/searchbatch.html";
    }

    // TODO: this needs to have the productPage stuff
    @GetMapping("/editform")
    String viewEditForm(
                    Model model,
                    
                    @RequestParam("editmode") String editmode,
                    @RequestParam("batchnumber") String batchnumber,
                    @RequestParam("page") Optional<Integer> page,
                    @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, null, false , false);
        return "checkout/editbatch.html";
    }

    @PostMapping("/filtereditform")
    String viewFilterEditForm(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);
        return "checkout/editbatch.html";
    }

    //    This will text the user a link to a menu pdf they can send people
    @PostMapping("/printmenu")
    String printmenu(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);
        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        batchControllerHelper.sendTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchnumber, 0.00)

        return "checkout/editbatch.html";
    }

    @PostMapping("/likesearch")
    String viewLikeSearch(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, true);
        return "checkout/editbatch.html";
    }

    // todo: add the pagination crap here too
    @PostMapping ("/editBatch")
    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                     @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
                     Model model,
                                HttpServletResponse response,
                                
                     @RequestParam("page") Optional<Integer> page,
                     @RequestParam("size") Optional<Integer> size
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateBatch.validateNewFormInfo(batchVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
            model.addAttribute("editmode","yes") //
        } else {

            BatchVO result = batchDao.updateBatch(batchVO);
           // BatchVO result = batchRepo.save(batchVO);

            // check to see if there are files uploaded related to this batchnumber
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), appConstants.UPLOAD_DIR);
            if(filelist.size() > 0){
                model.addAttribute("filelist", filelist);
            } else {
                model.addAttribute("filelist", null);
            }

            model.addAttribute("editmode","no") // after succesful edit is done we set this back to no
            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("batch", result);
        }

        batchControllerHelper.bindProducts(model, page, productTypeVO)
        batchControllerHelper.bindProductTypes(model)
        //  bindFilterProducts(model, page, productTypeVO)
        model.addAttribute("searchproducttype", new ProductTypeVO()) // this is a blank object for submitting a search term
        techvvsAuthService.checkuserauth(model)
        batchControllerHelper.bindBatchTypes(model)
        return "checkout/editbatch.html";
    }

    @PostMapping ("/createNewBatch")
    String createNewBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                                Model model,
                                HttpServletResponse response
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateBatch.validateNewFormInfo(batchVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            batchVO.setCreateTimeStamp(LocalDateTime.now());
            batchVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for batch types on the ui so we can save this batch object
            BatchVO result = batchRepo.save(batchVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("batch", result);
        }

        techvvsAuthService.checkuserauth(model)
        batchControllerHelper.bindBatchTypes(model)
        return "checkout/batch.html";
    }




}
