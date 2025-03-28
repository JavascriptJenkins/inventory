package com.techvvs.inventory.viewcontroller

import com.google.api.OAuthRequirementsOrBuilder
import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.security.rbac.RbacEnforcer
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.BatchService
import com.techvvs.inventory.service.controllers.ProductService
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.hibernate.engine.jdbc.batch.spi.Batch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.LocalDateTime


/*refactoring the dirty batch controller into this one*/
@RequestMapping("/batch")
@Controller
public class CleanBatchViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    ProductRepo productRepo

    @Autowired
    BatchDao batchDao;

    @Autowired
    ValidateBatch validateBatch;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    FormattingUtil formattingUtil

    @Autowired
    ControllerConstants controllerConstants
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    ProductTypeRepo productTypeRepo

    @Autowired
    BatchTypeRepo batchTypeRepo

    @Autowired
    ProductService productService

    @Autowired
    BatchService batchService

    @Autowired
    BarcodeGenerator barcodeGenerator

    @Autowired
    ProductHelper productHelper

    @Autowired
    RbacEnforcer rbacEnforcer



    @GetMapping
    String viewEditForm(
            Model model,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, null, false , false);
        return "batch/batch.html";
    }

    // uploadxlsx
    @GetMapping("/uploadxlsx")
    String viewUploadXslxNewBatch(
            @ModelAttribute( "batch" ) BatchVO batchVO,
                                  Model model
    ){



        BatchVO batchVOToBind;
        if(batchVO != null && batchVO.getBatchid() != null){
            batchVOToBind = batchVO;
        } else {
            batchVOToBind = new BatchVO();
            batchVOToBind.setBatchnumber(0);
            batchVOToBind.setBatchnumber(Integer.valueOf(batchControllerHelper.generateBatchNumber()));
//            batchVOToBind.setBatchnumber(secureRandom.nextInt(10000000));
            batchVOToBind.batch_type_id = new BatchTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", batchVOToBind);
        batchControllerHelper.bindBatchTypes(model)
        return "batch/xlsxbatch.html";
    }


    @GetMapping("/edit")
    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                       Model model,

                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size ){

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize, Sort.by(Sort.Direction.ASC, "batchid"));
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.ASC, "batchid"));
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
        model.addAttribute("batchPage", pageOfBatch);
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        return "batch/managebatch.html";
    }


    // todo: enforce admin rights to view this page
    // Admin page for editing the batch, removing and adding products and whatnot
    @GetMapping("/admin")
    String admin(@ModelAttribute( "batch" ) BatchVO batchVO,
                 Model model,
                 @RequestParam("batchid") Optional<Integer> batchid,
                 @RequestParam("productnamesearch") Optional<String> productnamesearch,
                 @RequestParam("productid") Optional<Integer> productid,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size,
                 HttpServletRequest req){


        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()
        Optional<ProductVO> product = Optional.empty()

        // bind the batch into scope
        if(batchid.isPresent()){
            batch = batchRepo.findById(batchid.get());
            model.addAttribute("batch", batch.get());
        }

        // bind the batch into scope
        if(productid.isPresent() && productid.get() != 0 && productid.get() != null) {
            product = productRepo.findById(productid.get());
            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("product", new ProductVO());
            model.addAttribute("editmode", false);
        }

        // now go get the list of paginated products in the batch



        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
                    model,
                    page,
                    size,
                    productnamesearch.get()
            )
        } else {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
        }

        bindBatchTypes(model)
        bindProductTypes(model)
        bindBatches(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/admin.html";
    }

    // This processes products on the batch/admin Administrate Batch Data Page
    @PostMapping("/admin/product/edit")
    String adminProductEdit(
            @ModelAttribute( "batch" ) BatchVO batchVO,
            @ModelAttribute( "product" ) ProductVO productVO,
                 Model model,
                 @RequestParam("batchid") Optional<Integer> batchid,
                 @RequestParam("productnamesearch") Optional<String> productnamesearch,
                 @RequestParam("productid") Optional<Integer> productid,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size,
                 HttpServletRequest req){


        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()
        Optional<ProductVO> product = Optional.empty()

        // bind the batch into scope
        if(batchid.isPresent()){
            batch = batchRepo.findById(batchid.get());
            model.addAttribute("batch", batch.get());
        }

        // bind the batch into scope
        if(
                productid.isPresent()
                && productid.get() != 0 &&
                productid.get() != null

        ) {
            // this means somebody selected a product from the table

            product = productRepo.findById(productid.get());
            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else if(productVO != null && productVO.product_id == 0){
            /* CREATE */
            product = Optional.of(productService.validateProductOnAdminBatchPage(productVO, model, true))

            // only proceed if there is no error
            if(model.getAttribute(MessageConstants.ERROR_MSG) == null){

                // create new batch
                BatchVO newBatch = batchService.createBatchRecord(productVO.batchname, productVO.batch_type_id)

                // generate time stamps and barcode
                productVO = generateTimestampsAndBarcode(productVO, newBatch)

                // add the batch to the product
                productVO.setBatch(newBatch);

                // create the product
                product = productService.createProduct(productVO)

                // add the product to the batch
                batch = Optional.of(batchService.addProductToBatch(product.get().batch, product.get()))


                model.addAttribute("successMessage", "Product and Batch created successfully!")
            }


            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else if(productVO != null && productVO.product_id > 0){
            /* UPDATE */
            // todo: implement product edit/update here.
            // steps:
            // validate all the fields (when validating price, make sure no sales have occured of this product)
            // don't allow updating of the product at all if any sales have occured of the product (except maybe cost, quantity, quantityremaining)
            // save the product.  (during product save, do the barcode generation etc same way xlsx import does it)

            product = Optional.of(productService.validateProductOnAdminBatchPage(productVO, model, false))

            // only proceed if there is no error
            if(model.getAttribute(MessageConstants.ERROR_MSG) == null){

//                // create new batch
//                BatchVO newBatch = batchService.createBatchRecord(productVO.batchname, productVO.batch_type_id)

                // update the product
                product = Optional.of(productService.saveProduct(product.get()))

                batch = Optional.of(productVO.batch)
                model.addAttribute("successMessage", "Product updated successfully!")
            }

            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("product", new ProductVO());
            model.addAttribute("editmode", false);
        }

        // now go get the list of paginated products in the batch



        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
                    model,
                    page,
                    size,
                    productnamesearch.get()
            )
        } else {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
        }

        bindProductTypes(model)
        bindBatchTypes(model)
        bindBatches(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/admin.html";
    }

    void bindProductTypes(Model model){
        // get all the producttype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypes", productTypeVOS);
    }

    void bindBatchTypes(Model model){
        // get all the batchtypes objects and bind them to select dropdown
        List<BatchTypeVO> batchTypeVOS = batchTypeRepo.findAll();
        model.addAttribute("batchtypes", batchTypeVOS);
    }

    void bindBatches(Model model){
        // get all the batchtypes objects and bind them to select dropdown
        List<BatchVO> batches = batchRepo.findAll();
        model.addAttribute("batches", batches);
    }

    ProductVO generateTimestampsAndBarcode(ProductVO productVO, BatchVO batchVO){
        productVO.updateTimeStamp = LocalDateTime.now()
        productVO.createTimeStamp = LocalDateTime.now()
        productVO.setProductnumber(Integer.valueOf(productHelper.generateProductNumber())); // ensure productnumber is unique
        productVO.quantityremaining = productVO.quantity
        ProductVO newSavedProduct = barcodeGenerator.generateAdhocBarcodeForProduct(productVO, batchVO)
        productVO.barcode = newSavedProduct.barcode
        return newSavedProduct
    }



    // todo: make sure this cannot be done for products that already have started selling...
    // Admin page for moving products between batches
    @GetMapping("/admin/move/product")
    String moveProduct(@ModelAttribute( "batch" ) BatchVO batchVO,
                 Model model,
                 @RequestParam("batchid") Optional<Integer> batchid,
                 @RequestParam("productnamesearch") Optional<String> productnamesearch,
                 @RequestParam("productid") Optional<Integer> productid,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size,
                 HttpServletRequest req){


        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()
        Optional<ProductVO> product = Optional.empty()

        // bind the batch into scope
        if(batchid.isPresent()){
            batch = batchRepo.findById(batchid.get());
            model.addAttribute("batch", batch.get());
        }

        Optional<String> filtertablename = Optional.empty()
        // bind the product into scope
        if(productid.isPresent() && productid.get() != 0 && productid.get() != null) {
            product = productRepo.findById(productid.get());
            filtertablename = Optional.of(product.get().name) // this will filter the table below
            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("product", new ProductVO());
            model.addAttribute("editmode", false);
        }

        // now go get the list of paginated products in the batch
        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
                    model,
                    page,
                    size,
                    productnamesearch.get()
            )
        } else if(filtertablename.isPresent() && !filtertablename.isEmpty()) {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindFilterProductsLikeSearchForMoveProductUI(model, page, size, filtertablename.get(), batch.get())
        } else {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
        }

        bindBatches(model)
        bindBatchTypes(model)
        bindProductTypes(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/adminmoveproduct.html";
    }


    // todo: make sure this cannot be done for products that already have started selling...
    // Admin page for moving products between batches
    @PostMapping("/admin/product/move")
    String moveProductPostMapping(@ModelAttribute( "batch" ) BatchVO batchVO,
                       Model model,
                       @RequestParam("batchid") Optional<Integer> batchid,
                       @RequestParam("productnamesearch") Optional<String> productnamesearch,
                       @RequestParam("productid") Optional<Integer> productid,
                       @ModelAttribute( "product" ) ProductVO productVO,
                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size,
                       HttpServletRequest req){


        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()
        Optional<ProductVO> product = Optional.empty()

        if(batchid.present){
            int targetbatchid = productVO.batch.batchid
            // here we have to parse the incoming productVO and see what the batchid is and move it
            batch = Optional.of(batchService.moveProductToNewBatch(batchid.get(), productVO))
            model.addAttribute("successMessage", "Product moved successfully from batch: "+batchid.get() + " to batch: "+targetbatchid)
        }

        // bind the batch into scope
        if(batchid.isPresent() && batch.isEmpty()){
            batch = batchRepo.findById(batchid.get());
            model.addAttribute("batch", batch.get());
        }

        // bind the product into scope
        if(productid.isPresent() && productid.get() != 0 && productid.get() != null) {
            product = productRepo.findById(productid.get());
            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("product", new ProductVO());
            model.addAttribute("editmode", false);
        }

        // now go get the list of paginated products in the batch
        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
                    model,
                    page,
                    size,
                    productnamesearch.get()
            )
        } else {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
        }

        bindBatches(model)
        bindBatchTypes(model)
        bindProductTypes(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/adminmoveproduct.html";
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
            pageable = PageRequest.of(0 , pageSize, Sort.by(Sort.Direction.DESC, "batchid"));
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "batchid"));
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
        model.addAttribute("batchPage", pageOfBatch);
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        return "batch/browsebatch.html";
    }

    @GetMapping("/searchBatch")
    String searchBatch(@ModelAttribute( "batch" ) BatchVO batchVO, Model model){



        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        model.addAttribute("batchs", new ArrayList<BatchVO>(1));
        return "batch/searchbatch.html";
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
        return "batch/searchbatch.html";
    }



    @PostMapping("/filtereditform")
    String viewFilterEditForm(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false);
        return "batch/batch.html";
    }

    //    This will text the user a link to a menu pdf they can send people
    @PostMapping("/printmenu")
    String printmenu(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false);
        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()

        boolean success = batchControllerHelper.sendTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchid, productTypeVO.priceadjustment)

        success ? model.addAttribute("successMessage", "Sent text message link successfully at: "+formattingUtil.getDateTimeForFileSystem()+" | With price adjustment:"+productTypeVO.priceadjustment) : model.addAttribute("errorMessage", "Error sending text message link at:" +formattingUtil.getDateTimeForFileSystem())

        return "batch/batch.html";
    }

    @PostMapping("/printmediamenu")
    String printmediamenu(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false);
        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()

        boolean success = batchControllerHelper.sendMediaTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchid, productTypeVO.priceadjustment)

        success ? model.addAttribute("successMessage", "Sent text message link successfully at: "+formattingUtil.getDateTimeForFileSystem()+" | With price adjustment:"+productTypeVO.priceadjustment) : model.addAttribute("errorMessage", "Error sending text message link at:" +formattingUtil.getDateTimeForFileSystem())

        return "batch/batch.html";
    }


    @PostMapping("/likesearch")
    String viewLikeSearch(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, true);
        return "batch/batch.html";
    }

    @PostMapping("/generatebarcodes")
    String viewGenerateBarcodes(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        boolean success = false

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.ALL:
                success = batchControllerHelper.generateAllBarcodesForBatch(batchid)
                break
            case controllerConstants.SINGLE_MENU:
                success = batchControllerHelper.generateSingleMenuBarcodesForBatch(batchid)
                break
        }



        if(success){
            model.addAttribute("successMessage","Successfully created barcodes for this batch. ")
        } else {
            model.addAttribute("errorMessage","Barcodes already exist for this batch. ")
        }


        return "batch/batch.html";
    }


    @PostMapping("/generateqrcodes")
    String viewGenerateQrcodes(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.ALL:
                batchControllerHelper.generateAllQrcodesForBatch(batchid)
                break
            case controllerConstants.SINGLE_MENU:
                batchControllerHelper.generateQrcodesForBatch(batchid)
                break
        }


        model.addAttribute("successMessage","Successfully created qr codes for this batch. ")

        return "batch/batch.html";
    }

    @PostMapping("/generateweightlabels")
    String generateWeightLabels(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.SINGLE_PAGE:
                batchControllerHelper.generateSinglePageWeightLabelsForBatch(batchid)
                break
        }


        model.addAttribute("successMessage","Successfully created qr codes for this batch. ")

        return "batch/editbatch.html";
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
        return "batch/editbatch.html";
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
        return "batch/batch.html";
    }




}
