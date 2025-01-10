package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.CrateService
import com.techvvs.inventory.service.controllers.DeliveryService
import com.techvvs.inventory.service.controllers.PackageService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.CrateHelper
import com.techvvs.inventory.viewcontroller.helper.DeliveryHelper
import com.techvvs.inventory.viewcontroller.helper.PackageHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

@RequestMapping("/delivery")
@Controller
public class DeliveryViewController {

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

    @Autowired
    DeliveryHelper deliveryHelper

    @Autowired
    DeliveryService deliveryService

    @Autowired
    BarcodeService barcodeService

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    SystemUserRepo systemUserRepo

    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("deliveryid") String deliveryid,
            @RequestParam("packageid") Optional<String> packageid,
            @RequestParam("crateid") Optional<String> crateid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("deliverypage") Optional<Integer> deliverypage,
            @RequestParam("deliverysize") Optional<Integer> deliverysize,
            @RequestParam("cratepage") Optional<Integer> cratepage,
            @RequestParam("cratesize") Optional<Integer> cratesize
    ){
        if(packageid.isPresent()){
            deliveryService.hadlePackageId(
                    packageid,
                    deliveryid,
                    deliverypage,
                    deliverysize,
                    model)

        } else if(crateid.isPresent()){
            deliveryService.hadleCrateId(
                    crateid,
                    deliveryid,
                    cratepage,
                    cratesize,
                    deliverypage,
                    deliverysize,
                    model)

        }else {
            model = deliveryHelper.loadDelivery(deliveryid, model, deliverypage, deliverysize)
        }
        DeliveryVO deliveryVO = (DeliveryVO) model.getAttribute("delivery")
        deliveryHelper.hydrateTransientQuantitiesForDisplay(deliveryVO)
        deliveryHelper.bindUnprocessedPackages(model, page, size) // bind all the unprocessed packages to the table for selection
        deliveryHelper.bindUnprocessedCrates(model, cratepage, cratesize) // bind all the unprocessed packages to the table for selection
        techvvsAuthService.checkuserauth(model)
        return "delivery/delivery.html";
    }

    //get the pending carts
    @GetMapping("pendingdeliveries")
    String viewPendingCrates(
            @ModelAttribute( "delivery" ) DeliveryVO deliveryVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // bind the page of packages
        deliveryHelper.findPendingDeliveries(model, page, size)

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("delivery", deliveryVO);
        return "delivery/pendingdeliveries.html";
    }


    // NOTE: this is not doing typical user auth
    //client view of readonly delivery status
    @GetMapping("item")
    String viewDeliveryForClient(
            Model model,
            @RequestParam("deliverytoken") Optional<String> deliverytoken,
            @RequestParam("deliveryid") Optional<String> deliveryid,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(deliverytoken.isPresent() && !menuid.isPresent()) {
            deliveryHelper.loadDeliveryByDeliveryToken(deliverytoken.get(), model)
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        // this is path for an internal user using a non-sms token to get access
        if(deliveryid.isPresent() && menuid.isPresent()){
            deliveryHelper.loadDeliveryByCustomParametersForInternalUser(
                    deliverytoken.get(), Integer.valueOf(deliveryid.get()), Integer.valueOf(menuid.get()), model)
            model.addAttribute("showbackbutton", "yes") // give internal users the back button
        }

        if(customerid.isPresent()){
            model.addAttribute("backButtonCustomerId", customerid.get())
        }

        if(page.isPresent() && size.isPresent()){
            model.addAttribute("backButtonPage", page.get())
            model.addAttribute("backButtonSize", size.get())
        }

        if(deliverytoken.present){
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }


        return "delivery/clientstatusview.html";
    }
    @GetMapping("queue")
    String getListOfPendingPickupAndDeliveryOrders(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("customerid") Optional<Integer> customerid,
    HttpServletRequest request
    ){
        techvvsAuthService.checkuserauth(model)

        Cookie[] cookies = request.getCookies();

        String token = ""
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("techvvs_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }


        // get a list of all the deliveries using pagination

        if(customerid.present && customerid.get() > 0) {
            deliveryHelper.findAllDeliveries(model, page, size, customerid)
        } else {
            deliveryHelper.findAllDeliveries(model, page, size, Optional.empty())
        }

        // pull the user who is currently logged in
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(authentication.getPrincipal().username)

//        public String createEmployeeDeliveryViewToken
//        (String email, List<Role> roles, int hours, String menuid, String customerid, String deliveryid) {

//        List<Role> roles = Arrays.asList(Role.ROLE_CLIENT, Role.ROLE_DELIVERY_VIEW_TOKEN);
//        String token = jwtTokenProvider.createEmployeeDeliveryViewTokenForInternalCustomer(systemUserDAO.email, roles, 96,
//                menuid.get(), String.valueOf(transactionVO.customervo.customerid), String.valueOf(transactionVO.delivery.deliveryid))


        model.addAttribute("deliverytoken", token)
        checkoutHelper.getAllCustomers(model)

        return "delivery/deliveryqueue.html";
    }
    @PostMapping("/status/prep")
    String changeStatusToEnPrep(
            Model model,
            @RequestParam("deliverytoken") Optional<String> deliverytoken,
            @RequestParam("deliveryid") Optional<String> deliveryid,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(deliverytoken.isPresent() && !menuid.isPresent()) {
            deliveryHelper.changeStatusToPrep(deliverytoken.get(), model)
            deliveryHelper.loadDeliveryByDeliveryToken(deliverytoken.get(), model)
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        // this is path for an internal user using a non-sms token to get access
        if(deliveryid.isPresent() && menuid.isPresent()){
            jwtTokenProvider.validateTokenSimple(deliverytoken.get())
            deliveryHelper.changeStatusToPrepForInternalUser(deliveryid.get())
            deliveryHelper.loadDeliveryByCustomParametersForInternalUser(
                    deliverytoken.get(), Integer.valueOf(deliveryid.get()), Integer.valueOf(menuid.get()), model)
            model.addAttribute("showbackbutton", "yes") // give internal users the back button
        }

        if(customerid.isPresent()){
            model.addAttribute("backButtonCustomerId", customerid.get())
        }

        if(page.isPresent() && size.isPresent()){
            model.addAttribute("backButtonPage", page.get())
            model.addAttribute("backButtonSize", size.get())
        }

        if(deliverytoken.present){
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        return "delivery/clientstatusview.html";
    }

    @PostMapping("/status/dispatch")
    String changeStatusToDispatch(
            Model model,
            @RequestParam("deliverytoken") Optional<String> deliverytoken,
            @RequestParam("deliveryid") Optional<String> deliveryid,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(deliverytoken.isPresent() && !menuid.isPresent()) {
            deliveryHelper.changeStatusToDispatch(deliverytoken.get(), model)
            deliveryHelper.loadDeliveryByDeliveryToken(deliverytoken.get(), model)
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        // this is path for an internal user using a non-sms token to get access
        if(deliveryid.isPresent() && menuid.isPresent()){
            jwtTokenProvider.validateTokenSimple(deliverytoken.get())
            deliveryHelper.changeStatusToDispatchForInternalUser(deliveryid.get())
            deliveryHelper.loadDeliveryByCustomParametersForInternalUser(
                    deliverytoken.get(), Integer.valueOf(deliveryid.get()), Integer.valueOf(menuid.get()), model)
            model.addAttribute("showbackbutton", "yes") // give internal users the back button
        }

        if(customerid.isPresent()){
            model.addAttribute("backButtonCustomerId", customerid.get())
        }

        if(page.isPresent() && size.isPresent()){
            model.addAttribute("backButtonPage", page.get())
            model.addAttribute("backButtonSize", size.get())
        }

        if(deliverytoken.present){
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        return "delivery/clientstatusview.html";
    }

    @PostMapping("/status/enroute")
    String changeStatusToEnRoute(
            Model model,
            @RequestParam("deliverytoken") Optional<String> deliverytoken,
            @RequestParam("deliveryid") Optional<String> deliveryid,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(deliverytoken.isPresent() && !menuid.isPresent()) {
            deliveryHelper.changeStatusToEnroute(deliverytoken.get(), model)
            deliveryHelper.loadDeliveryByDeliveryToken(deliverytoken.get(), model)
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        // this is path for an internal user using a non-sms token to get access
        if(deliveryid.isPresent() && menuid.isPresent()){
            jwtTokenProvider.validateTokenSimple(deliverytoken.get())
            deliveryHelper.changeStatusToEnrouteForInternalUser(deliveryid.get())
            deliveryHelper.loadDeliveryByCustomParametersForInternalUser(
                    deliverytoken.get(), Integer.valueOf(deliveryid.get()), Integer.valueOf(menuid.get()), model)
            model.addAttribute("showbackbutton", "yes") // give internal users the back button
        }

        if(customerid.isPresent()){
            model.addAttribute("backButtonCustomerId", customerid.get())
        }

        if(page.isPresent() && size.isPresent()){
            model.addAttribute("backButtonPage", page.get())
            model.addAttribute("backButtonSize", size.get())
        }

        if(deliverytoken.present){
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        return "delivery/clientstatusview.html";
    }

    @PostMapping("/status/complete")
    String changeStatusToComplete(
            Model model,
            @RequestParam("deliverytoken") Optional<String> deliverytoken,
            @RequestParam("deliveryid") Optional<String> deliveryid,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(deliverytoken.isPresent() && !menuid.isPresent()) {
            deliveryHelper.changeStatusToComplete(deliverytoken.get(), model)
            deliveryHelper.loadDeliveryByDeliveryToken(deliverytoken.get(), model)
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        // this is path for an internal user using a non-sms token to get access
        if(deliveryid.isPresent() && menuid.isPresent()){
            jwtTokenProvider.validateTokenSimple(deliverytoken.get())
            deliveryHelper.changeStatusToCompleteForInternalUser(deliveryid.get())
            deliveryHelper.loadDeliveryByCustomParametersForInternalUser(
                    deliverytoken.get(), Integer.valueOf(deliveryid.get()), Integer.valueOf(menuid.get()), model)
            model.addAttribute("showbackbutton", "yes") // give internal users the back button
        }

        if(customerid.isPresent()){
            model.addAttribute("backButtonCustomerId", customerid.get())
        }

        if(page.isPresent() && size.isPresent()){
            model.addAttribute("backButtonPage", page.get())
            model.addAttribute("backButtonSize", size.get())
        }

        if(deliverytoken.present){
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        return "delivery/clientstatusview.html";
    }

    @GetMapping("item/ajax/status")
    @ResponseBody // this returns plain string instead of view
    String getDeliveryStatus(
            Model model,
            @RequestParam("deliverytoken") Optional<String> deliverytoken,
            @RequestParam("deliveryid") Optional<String> deliveryid,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(deliverytoken.isPresent() && !menuid.present) {
            return String.valueOf(deliveryHelper.returnDeliveryStatus(deliverytoken.get(), model))
        }

        // todo: split all these internal user paths into a method
        // this is path for an internal user using a non-sms token to get access
        if(deliveryid.isPresent() && menuid.isPresent()){
            deliveryHelper.loadDeliveryByCustomParametersForInternalUser(
                    deliverytoken.get(), Integer.valueOf(deliveryid.get()), Integer.valueOf(menuid.get()), model)
            model.addAttribute("showbackbutton", "yes") // give internal users the back button
        }

        if(customerid.isPresent()){
            model.addAttribute("backButtonCustomerId", customerid.get())
        }

        if(page.isPresent() && size.isPresent()){
            model.addAttribute("backButtonPage", page.get())
            model.addAttribute("backButtonSize", size.get())
        }

        if(deliverytoken.present){
            deliveryHelper.bindHiddenValues(model, deliverytoken.get())
        }

        return "0"
    }

    // todo: write in a validation check to make sure you can't add more than is available in the batch
    // first user creates a cart by scanning items.
    // on next page "final package", the cart will be transformed into a transaction
    @PostMapping("/scan")
    String scan(@ModelAttribute( "delivery" ) DeliveryVO deliveryVO,
                Model model,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size,
                @RequestParam("deliverypage") Optional<Integer> deliverypage,
                @RequestParam("deliverysize") Optional<Integer> deliverysize,
                @RequestParam("cratepage") Optional<Integer> cratepage,
                @RequestParam("cratesize") Optional<Integer> cratesize

    ){


        // check here to see if incoming deliveryid already exists - idk if we need to

        deliveryVO = deliveryHelper.validateDeliveryReviewVO(deliveryVO, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one

            deliveryVO = deliveryService.saveDeliveryIfNew(deliveryVO)

            //  if the transactionVO comes back here without a
            // after transaction is created, search for the product based on barcode

            // determine if the barcode is a package or delivery type of object
            String type = barcodeService.determineTypeOfBarcode(deliveryVO.barcode)


            if(type == "package"){
                deliveryVO = deliveryService.searchForPackageByBarcodeAndDelivery(deliveryVO, model, page, size)
            }

            if(type == "crate"){
                deliveryVO = deliveryService.searchForCrateByBarcodeAndDelivery(deliveryVO, model, page, size)
            }

            if(type == "NOTFOUND" && deliveryVO.package_list.size() > 0 || deliveryVO.crate_list.size() > 0){
                model.addAttribute("errorMessage","UPCA Barcode not found in system")
            }

            // This means it is the first time the delivery is being created
            if(type == "NOTFOUND" && deliveryVO.package_list.size() == 0 && deliveryVO.crate_list.size() == 0){
                model.addAttribute("successMessage","Delivery created with name: "+deliveryVO.name)
            }

        }

        model = deliveryHelper.loadDelivery(String.valueOf(deliveryVO.deliveryid), model, deliverypage, deliverysize)

        deliveryVO = deliveryHelper.hydrateTransientQuantitiesForDisplay((DeliveryVO) model.getAttribute("delivery"))


        deliveryVO.barcode = "" // reset barcode to empty

        deliveryHelper.bindUnprocessedPackages(model, page, size) // bind all the unprocessed packages to the table for selection
        deliveryHelper.bindUnprocessedCrates(model, page, size) // bind all the unprocessed packages to the table for selection
        deliveryHelper.bindPackagesInDelivery(model, deliverypage, deliverysize, deliveryVO)
        deliveryHelper.bindCratesInDelivery(model, cratepage, cratesize, deliveryVO)
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("delivery", deliveryVO);

        return "delivery/delivery.html";
    }

    @PostMapping("/deleteitem")
    String deleteitem(
                Model model,
                
                @RequestParam("deliveryid") String deliveryid,
                @RequestParam("barcode") String barcode,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size,
                @RequestParam("page") Optional<Integer> deliverypage,
                @RequestParam("size") Optional<Integer> deliverysize,
                @RequestParam("cratepage") Optional<Integer> cratepage,
                @RequestParam("cratesize") Optional<Integer> cratesize){

        // determine if the barcode is a package or delivery type of object
        String type = barcodeService.determineTypeOfBarcode(barcode)

        DeliveryVO deliveryVO = deliveryHelper.getExistingDelivery(deliveryid)

        if(type == "package"){
            deliveryVO = deliveryService.deletePackageFromDelivery(deliveryVO, barcode)
        }

        if(type == "crate"){
            deliveryVO = deliveryService.deleteCrateFromDelivery(deliveryVO, barcode)
        }

        if(type == "NOTFOUND"){
            model.addAttribute("errorMessage","UPCA Barcode not found in system")
        }

        deliveryVO = deliveryHelper.hydrateTransientQuantitiesForDisplay(deliveryVO)
        deliveryVO.barcode = "" // reset barcode to empty

        deliveryHelper.bindUnprocessedPackages(model, page, size) // bind all the unprocessed packages to the table for selection
        deliveryHelper.bindUnprocessedCrates(model, page, size) // bind all the unprocessed packages to the table for selection
        deliveryHelper.bindPackagesInDelivery(model, deliverypage, deliverysize, deliveryVO)
        deliveryHelper.bindCratesInDelivery(model, cratepage, cratesize, deliveryVO)
        techvvsAuthService.checkuserauth(model)

        model.addAttribute("delivery", deliveryVO);

        return "delivery/delivery.html";
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
