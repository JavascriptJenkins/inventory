package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.CrateService
import com.techvvs.inventory.service.controllers.PackageService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.viewcontroller.helper.CrateHelper
import com.techvvs.inventory.viewcontroller.helper.PackageHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@RequestMapping("/crate")
@Controller
public class CrateViewController {

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    AppConstants appConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    PackageHelper packageHelper

    @Autowired
    CrateHelper crateHelper

    @Autowired
    CrateService crateService

    @Autowired
    PackageRepo packageRepo

    @Autowired
    PackageService packageService

    @Autowired
    TransactionService transactionService

    @Autowired
    PrinterService printerService
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("crateid") String crateid,
            @RequestParam("packageid") Optional<String> packageid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        if(packageid.isPresent()){
            model = packageHelper.loadPackage(packageid.get(), model)
            // check to see if this package is already in a crate
            PackageVO packageVO = (PackageVO) model.getAttribute("package")
            if(packageVO.crate != null){
                // load the associated crate for the package if it exists
                model = crateHelper.loadCrate(String.valueOf(packageVO.crate.crateid), model)
            } else {
                model = crateHelper.loadCrate(crateid, model)
                CrateVO crateVO = (CrateVO) model.getAttribute("crate")

                crateVO.packageinscope = packageVO // if it's first time navigating from package create page, add the package to the packageinscope
            }
        } else {
            model = crateHelper.loadCrate(crateid, model)
        }
        crateHelper.bindPackages(model, page, size) // bind all the unprocessed packages to the table for selection
        techvvsAuthService.checkuserauth(model)
        return "crate/crate.html";
    }

    //get the pending carts
    @GetMapping("pendingcrates")
    String viewPendingCrates(
            @ModelAttribute( "crate" ) CrateVO crateVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // bind the page of packages
        crateHelper.findPendingCrates(model, page, size)

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("crate", crateVO);
        return "crate/pendingcrates.html";
    }

    // todo: write in a validation check to make sure you can't add more than is available in the batch
    // first user creates a cart by scanning items.
    // on next page "final package", the cart will be transformed into a transaction
    @PostMapping("/scan")
    String scan(@ModelAttribute( "crate" ) CrateVO crateVO,
                Model model,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size){


        // check here to see if incoming crateid already exists - idk if we need to

        crateVO = crateHelper.validateCrateReviewVO(crateVO, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one

            crateVO = crateService.saveCrateIfNew(crateVO)

            //  if the transactionVO comes back here without a
            // after transaction is created, search for the product based on barcode

            crateVO = crateService.searchForPackageByBarcodeAndCrate(crateVO, model, page, size)


        }

        crateVO = crateHelper.hydrateTransientQuantitiesForDisplay(crateVO)


        crateVO.barcode = "" // reset barcode to empty

        crateHelper.bindPackages(model, page, size) // bind all the unprocessed packages to the table for selection
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("crate", crateVO);

        return "crate/crate.html";
    }

    @PostMapping("/deletepackage")
    String deletepackage(
                Model model,
                
                @RequestParam("crateid") String crateid,
                @RequestParam("barcode") String barcode,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size){

        

        CrateVO crateVO = crateHelper.getExistingCrate(crateid)

        crateVO = crateService.deletePackageFromCrate(crateVO, barcode)

        crateVO = crateHelper.hydrateTransientQuantitiesForDisplay(crateVO)

        crateVO.barcode = "" // reset barcode to empty

        crateHelper.bindPackages(model, page, size) // bind all the unprocessed packages to the table for selection
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("crate", crateVO);

        return "crate/crate.html";
    }


    @PostMapping("/reviewpackage")
    String reviewpackage(@ModelAttribute( "package" ) PackageVO packageVO,
                Model model,
                
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size){



        packageVO = packageHelper.validatePackageReviewVO(packageVO, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one
            packageVO = packageHelper.getExistingPackage(String.valueOf(packageVO.packageid))

            packageVO = packageHelper.hydrateTransientQuantitiesForDisplay(packageVO)

        }

        packageVO.barcode = "" // reset barcode to empty

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("package", packageVO);
        model.addAttribute("successMessage", "Review the package")
        // fetch all customers from database and bind them to model
        packageHelper.getAllPackageTypes(model)

        return "package/reviewpackage.html";
    }

    // this will process the cart and create transaction records
    @PostMapping("/transaction")
    String transaction(@ModelAttribute( "package" ) PackageVO packageVO,
                      Model model,
                      @RequestParam("page") Optional<Integer> page,
                      @RequestParam("size") Optional<Integer> size){



        packageVO = packageHelper.validatePackageReviewVO(packageVO, model)
        TransactionVO transactionVO = new TransactionVO()


        String name = ""

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){

            packageVO = packageHelper.getExistingPackage(String.valueOf(packageVO.packageid))

            name = packageVO.name

            // save a new transaction object in database if we don't have one
            transactionVO = transactionService.processCartGenerateNewTransaction(packageVO)
        }



        // fetch all customers from database and bind them to model
        packageHelper.getAllCustomers(model)

        techvvsAuthService.checkuserauth(model)


        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully completed transaction! Thanks "+name+"!")


            // todo: this needs to look in package directory of transaction
            // start file paging
            String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
            Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
            filePagingService.bindPageAttributesToModel(model, filePage, page, size);
            // end file paging

            transactionVO = packageHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

            printerService.printInvoice(transactionVO, false, true)

            model.addAttribute("transaction", transactionVO);
            return "package/transactionreview.html"; // todo: figure this out

        } else {
            return "package/transaction.html";// return same page with errors
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

            transactionVO = packageHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

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
        packageHelper.getAllCustomers(model)

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

            transactionVO = packageHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

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
        packageHelper.getAllCustomers(model)

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

        transactionVO = packageHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        packageHelper.bindtransients(transactionVO, transientphonunumber, transientemail, transientaction)

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
        packageHelper.getAllCustomers(model)

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

        transactionVO = packageHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO)

        packageHelper.bindtransients(transactionVO, transientphonunumber, transientemail, transientaction)
        printerService.printInvoice(transactionVO, false, false)


        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.TRANSACTION_INVOICE_DIR+String.valueOf(transactionVO.transactionid)+"/"
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("transaction", transactionVO);
        packageHelper.getAllCustomers(model)

        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully sent invoice! Thanks!")

            return "transaction/transactionreview.html";

        } else {
            return "transaction/transactionreview.html";// return same page with errors
        }

    }

    @GetMapping("/browsePackage")
    String browsePackage(@ModelAttribute( "package" ) PackageVO packageVO,
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

        Page<PackageVO> pageOfProduct = packageRepo.findAll(pageable);

        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("package", new PackageVO());
        model.addAttribute("packagePage", pageOfProduct);
        return "package/browsepackage.html";
    }

    @GetMapping("/searchPackage")
    String searchPackage(@ModelAttribute( "package" ) PackageVO packageVO, Model model){

        

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("package", new PackageVO());
        model.addAttribute("packages", new ArrayList<PackageVO>(1));
        return "package/searchpackage.html";
    }

    @PostMapping("/searchPackage")
    String searchPackagePost(@ModelAttribute( "package" ) PackageVO packageVO, Model model){

        

        List<PackageVO> results = new ArrayList<PackageVO>();
        if(packageVO.getName() != null && results.size() == 0){
            System.out.println("Searching data by getName");
            results = packageRepo.findAllByName(packageVO.getName());
        }
        if(packageVO.getDescription() != null && results.size() == 0){
            System.out.println("Searching data by getDescription");
            results = packageRepo.findAllByDescription(packageVO.getDescription());
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("package", packageVO);
        model.addAttribute("packages", results);
        return "package/searchpackage.html";
    }








}
