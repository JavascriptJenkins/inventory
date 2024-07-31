package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
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

@RequestMapping("/menu")
@Controller
public class MenuViewController {


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


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("menuid") String menuid,
            @RequestParam("cartid") String cartid,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){



        menuVO = menuHelper.loadMenu(menuid, model)
        model = checkoutHelper.loadCart(cartid, model, cartVO, menuid)



        // todo: add a button on the ui to pull the latest transaction for customer (so if someone clicks off page
        //  you can come back and finish the transaction)

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);



        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        model.addAttribute("customJwtParameter", customJwtParameter);
        return "menu/menu.html";
    }


    @PostMapping("/scan")
    String scan(@ModelAttribute( "cart" ) CartVO cartVO,
                Model model,
                @RequestParam("customJwtParameter") String customJwtParameter,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size
    ){

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);

        cartVO = menuHelper.validateMenuPageCartVO(cartVO, model)

        String menuid = cartVO.menuid


        MenuVO menuVO = menuHelper.loadMenu(menuid, model)

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one

            cartVO = checkoutHelper.saveCartIfNew(cartVO)

            if(cartVO.barcode != null && cartVO.barcode != ""){
                     cartVO = cartService.searchForProductByBarcode(cartVO, model, page, size)
            }



        }

        cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)

        cartVO.barcode = "" // reset barcode to empty
        cartVO.menuid = menuid

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("cart", cartVO);
        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)

        return "menu/menu.html";
    }




//
//    //get the pending transactions
//    @GetMapping("pendingtransactions")
//    String viewPendingTransactions(
//            @ModelAttribute( "transaction" ) TransactionVO transactionVO,
//            Model model,
//            @RequestParam("customJwtParameter") String customJwtParameter,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        // bind the page of transactions
//        checkoutHelper.findPendingTransactions(model, page, size)
//        // fetch all customers from database and bind them to model
//        checkoutHelper.getAllCustomers(model)
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("transaction", transactionVO);
//        return "checkout/pendingtransactions.html";
//    }
//
//    //get the pending carts
//    @GetMapping("pendingcarts")
//    String viewPendingTransactions(
//            @ModelAttribute( "cart" ) CartVO cartVO,
//            Model model,
//            @RequestParam("customJwtParameter") String customJwtParameter,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        // bind the page of transactions
//        checkoutHelper.findPendingCarts(model, page, size)
//        // fetch all customers from database and bind them to model
//        checkoutHelper.getAllCustomers(model)
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("cart", cartVO);
//        return "checkout/pendingcarts.html";
//    }
//
//    // todo: write in a validation check to make sure you can't add more than is available in the batch
//    // first user creates a cart by scanning items.
//    // on next page "final checkout", the cart will be transformed into a transaction
//    @PostMapping("/scan")
//    String scan(@ModelAttribute( "cart" ) CartVO cartVO,
//                Model model,
//                @RequestParam("customJwtParameter") String customJwtParameter,
//                @RequestParam("page") Optional<Integer> page,
//                @RequestParam("size") Optional<Integer> size){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        cartVO = checkoutHelper.validateCartVO(cartVO, model)
//
//        // only proceed if there is no error
//        if(model.getAttribute("errorMessage") == null){
//            // save a new transaction object in database if we don't have one
//
//            cartVO = checkoutHelper.saveCartIfNew(cartVO)
//
//            //  if the transactionVO comes back here without a
//            // after transaction is created, search for the product based on barcode
//
//            cartVO = cartService.searchForProductByBarcode(cartVO, model, page, size)
//
//
//        }
//
//        cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)
//
//        cartVO.barcode = "" // reset barcode to empty
////
////        model.addAttribute("pageNumbers", pageNumbers);
////        model.addAttribute("page", currentPage);
////        model.addAttribute("size", pageOfProduct.getTotalPages());
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("cart", cartVO);
//        // fetch all customers from database and bind them to model
//        checkoutHelper.getAllCustomers(model)
//
//        return "checkout/checkout.html";
//    }
//
//
//    @PostMapping("/reviewcart")
//    String reviewcart(@ModelAttribute( "cart" ) CartVO cartVO,
//                Model model,
//                @RequestParam("customJwtParameter") String customJwtParameter,
//                @RequestParam("page") Optional<Integer> page,
//                @RequestParam("size") Optional<Integer> size){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        cartVO = checkoutHelper.validateCartReviewVO(cartVO, model)
//
//        // only proceed if there is no error
//        if(model.getAttribute("errorMessage") == null){
//            // save a new transaction object in database if we don't have one
//            cartVO = checkoutHelper.getExistingCart(String.valueOf(cartVO.cartid))
//
//
//            // bind objects for reviewing the cart
//            //cartVO = checkoutHelper.reviewCart(cartVO, model)
//
//            //  if the transactionVO comes back here without a
//            // after transaction is created, search for the product based on barcode
//
////            cartVO = checkoutHelper.searchForProductByBarcode(cartVO, model, page, size)
//            cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)
//
//        }
//
//        cartVO.barcode = "" // reset barcode to empty
////
////        model.addAttribute("pageNumbers", pageNumbers);
////        model.addAttribute("page", currentPage);
////        model.addAttribute("size", pageOfProduct.getTotalPages());
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("cart", cartVO);
//        model.addAttribute("successMessage", "Review the cart")
//        // fetch all customers from database and bind them to model
//        checkoutHelper.getAllCustomers(model)
//
//        return "checkout/reviewcart.html";
//    }
//
//    // this will process the cart and create transaction records
//    @PostMapping("/transaction")
//    String transaction(@ModelAttribute( "cart" ) CartVO cartVO,
//                      Model model,
//                      @RequestParam("customJwtParameter") String customJwtParameter,
//                      @RequestParam("page") Optional<Integer> page,
//                      @RequestParam("size") Optional<Integer> size){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        cartVO = checkoutHelper.validateCartReviewVO(cartVO, model)
//        TransactionVO transactionVO = new TransactionVO()
//
//
//        String name = ""
//
//        // only proceed if there is no error
//        if(model.getAttribute("errorMessage") == null){
//
//            cartVO = cartService.getExistingCart(cartVO)
//
//
//            name =cartVO.customer.name
//
//            // save a new transaction object in database if we don't have one
//            transactionVO = transactionService.processCartGenerateNewTransaction(cartVO)
//
//            cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(transactionVO.cart)
//
//
//        }
//
////
////        model.addAttribute("pageNumbers", pageNumbers);
////        model.addAttribute("page", currentPage);
////        model.addAttribute("size", pageOfProduct.getTotalPages());
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("transaction", transactionVO);
//        //model.addAttribute("successMessage", "Review the cart")
//        // fetch all customers from database and bind them to model
//        checkoutHelper.getAllCustomers(model)
//
//        if(model.getAttribute("errorMessage") == null){
//            model.addAttribute("successMessage", "Successfully completed transaction! Thanks "+name+"!")
//
//            return "checkout/transactionsuccess.html";
//
//        } else {
//            return "checkout/transaction.html";// return same page with errors
//        }
//
//    }
//
//
//    @PostMapping("/printreceipt")
//    String printreceipt(@ModelAttribute( "transaction" ) TransactionVO transactionVO,
//                       Model model,
//                       @RequestParam("customJwtParameter") String customJwtParameter,
//                       @RequestParam("page") Optional<Integer> page,
//                       @RequestParam("size") Optional<Integer> size){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//       // cartVO = checkoutHelper.validateCartReviewVO(transactionVO, model)
//       // TransactionVO transactionVO = new TransactionVO()
//
//
//        String name = ""
//
//        // only proceed if there is no error
//        if(model.getAttribute("errorMessage") == null){
//
//            transactionVO = transactionService.getExistingTransaction(transactionVO.transactionid)
//
//
//            CartVO cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(transactionVO.cart)
//
//
//            printerService.printReceipt(transactionVO)
//
//            name = transactionVO.cart.customer.name
//
//
//        //    CartVO cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(transactionVO.cart)
//
//
//        }
//
////
////        model.addAttribute("pageNumbers", pageNumbers);
////        model.addAttribute("page", currentPage);
////        model.addAttribute("size", pageOfProduct.getTotalPages());
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("transaction", transactionVO);
//        //model.addAttribute("successMessage", "Review the cart")
//        // fetch all customers from database and bind them to model
//        checkoutHelper.getAllCustomers(model)
//
//        if(model.getAttribute("errorMessage") == null){
//            model.addAttribute("successMessage", "Successfully printed receipt! Thanks "+name+"!")
//
//            return "checkout/transactionsuccess.html";
//
//        } else {
//            return "checkout/transactionsuccess.html";// return same page with errors
//        }
//
//    }
//
//
//    @GetMapping("/browseBatch")
//    String browseBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
//                             Model model,
//                             @RequestParam("customJwtParameter") String customJwtParameter,
//                             @RequestParam("page") Optional<Integer> page,
//                             @RequestParam("size") Optional<Integer> size ){
//
//        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
//        //pagination
//        int currentPage = page.orElse(0);
//        int pageSize = 5;
//        Pageable pageable;
//        if(currentPage == 0){
//            pageable = PageRequest.of(0 , pageSize);
//        } else {
//            pageable = PageRequest.of(currentPage - 1, pageSize);
//        }
//
//        Page<BatchVO> pageOfBatch = batchRepo.findAll(pageable);
//
//        int totalPages = pageOfBatch.getTotalPages();
//
//        List<Integer> pageNumbers = new ArrayList<>();
//
//        while(totalPages > 0){
//            pageNumbers.add(totalPages);
//            totalPages = totalPages - 1;
//        }
//
//        model.addAttribute("pageNumbers", pageNumbers);
//        model.addAttribute("page", currentPage);
//        model.addAttribute("size", pageOfBatch.getTotalPages());
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("batch", new BatchVO());
//        model.addAttribute("batchPage", pageOfBatch);
//        return "checkout/browsebatch.html";
//    }
//
//    @GetMapping("/searchBatch")
//    String searchBatch(@ModelAttribute( "batch" ) BatchVO batchVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("batch", new BatchVO());
//        model.addAttribute("batchs", new ArrayList<BatchVO>(1));
//        return "checkout/searchbatch.html";
//    }
//
//    @PostMapping("/searchBatch")
//    String searchBatchPost(@ModelAttribute( "batch" ) BatchVO batchVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){
//
//        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);
//
//        List<BatchVO> results = new ArrayList<BatchVO>();
//        if(batchVO.getBatchnumber() != null){
//            System.out.println("Searching data by getBatchnumber");
//            results = batchRepo.findAllByBatchnumber(batchVO.getBatchnumber());
//        }
//        if(batchVO.getName() != null && results.size() == 0){
//            System.out.println("Searching data by getName");
//            results = batchRepo.findAllByName(batchVO.getName());
//        }
//        if(batchVO.getDescription() != null && results.size() == 0){
//            System.out.println("Searching data by getDescription");
//            results = batchRepo.findAllByDescription(batchVO.getDescription());
//        }
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("batch", batchVO);
//        model.addAttribute("batchs", results);
//        return "checkout/searchbatch.html";
//    }
//
//    // TODO: this needs to have the productPage stuff
//    @GetMapping("/editform")
//    String viewEditForm(
//                    Model model,
//                    @RequestParam("customJwtParameter") String customJwtParameter,
//                    @RequestParam("editmode") String editmode,
//                    @RequestParam("batchnumber") String batchnumber,
//                    @RequestParam("page") Optional<Integer> page,
//                    @RequestParam("size") Optional<Integer> size
//
//    ){
//
//        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, null, false , false);
//        return "checkout/editbatch.html";
//    }
//
//    @PostMapping("/filtereditform")
//    String viewFilterEditForm(
//            Model model,
//            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
//            @RequestParam("customJwtParameter") String customJwtParameter,
//            @RequestParam("editmode") String editmode,
//            @RequestParam("batchnumber") String batchnumber,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//
//    ){
//
//        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, false);
//        return "checkout/editbatch.html";
//    }
//
//    //    This will text the user a link to a menu pdf they can send people
//    @PostMapping("/printmenu")
//    String printmenu(
//            Model model,
//            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
//            @RequestParam("customJwtParameter") String customJwtParameter,
//            @RequestParam("editmode") String editmode,
//            @RequestParam("batchnumber") String batchnumber,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ){
//
//        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, false);
//        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user
//
//
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        batchControllerHelper.sendTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchnumber)
//
//        return "checkout/editbatch.html";
//    }
//
//    @PostMapping("/likesearch")
//    String viewLikeSearch(
//            Model model,
//            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
//            @RequestParam("customJwtParameter") String customJwtParameter,
//            @RequestParam("editmode") String editmode,
//            @RequestParam("batchnumber") String batchnumber,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//
//    ){
//
//        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, true);
//        return "checkout/editbatch.html";
//    }
//
//    // todo: add the pagination crap here too
//    @PostMapping ("/editBatch")
//    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
//                     @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
//                     Model model,
//                                HttpServletResponse response,
//                                @RequestParam("customJwtParameter") String customJwtParameter,
//                     @RequestParam("page") Optional<Integer> page,
//                     @RequestParam("size") Optional<Integer> size
//    ){
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("----------------------- START AUTH INFO ");
//        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
//        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
//        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
//        System.out.println("----------------------- END AUTH INFO ");
//
//        String errorResult = validateBatch.validateNewFormInfo(batchVO);
//
//        // Validation
//        if(!errorResult.equals("success")){
//            model.addAttribute("errorMessage",errorResult);
//            model.addAttribute("editmode","yes") //
//        } else {
//
//            BatchVO result = batchDao.updateBatch(batchVO);
//           // BatchVO result = batchRepo.save(batchVO);
//
//            // check to see if there are files uploaded related to this batchnumber
//            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), appConstants.UPLOAD_DIR);
//            if(filelist.size() > 0){
//                model.addAttribute("filelist", filelist);
//            } else {
//                model.addAttribute("filelist", null);
//            }
//
//            model.addAttribute("editmode","no") // after succesful edit is done we set this back to no
//            model.addAttribute("successMessage","Record Successfully Saved.");
//            model.addAttribute("batch", result);
//        }
//
//        batchControllerHelper.bindProducts(model, page, productTypeVO)
//        batchControllerHelper.bindProductTypes(model)
//        //  bindFilterProducts(model, page, productTypeVO)
//        model.addAttribute("searchproducttype", new ProductTypeVO()) // this is a blank object for submitting a search term
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        batchControllerHelper.bindBatchTypes(model)
//        return "checkout/editbatch.html";
//    }
//
//    @PostMapping ("/createNewBatch")
//    String createNewBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
//                                Model model,
//                                HttpServletResponse response,
//                                @RequestParam("customJwtParameter") String customJwtParameter
//    ){
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("----------------------- START AUTH INFO ");
//        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
//        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
//        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
//        System.out.println("----------------------- END AUTH INFO ");
//
//        String errorResult = validateBatch.validateNewFormInfo(batchVO);
//
//        // Validation
//        if(!errorResult.equals("success")){
//            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
//            model.addAttribute("errorMessage",errorResult);
//        } else {
//
//            // when creating a new processData entry, set the last attempt visit to now - this may change in future
//            batchVO.setCreateTimeStamp(LocalDateTime.now());
//            batchVO.setUpdateTimeStamp(LocalDateTime.now());
//
//
//            //todo: add support for batch types on the ui so we can save this batch object
//            BatchVO result = batchRepo.save(batchVO);
//
//            model.addAttribute("successMessage","Record Successfully Saved. ");
//            model.addAttribute("batch", result);
//        }
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        batchControllerHelper.bindBatchTypes(model)
//        return "checkout/batch.html";
//    }
//



}