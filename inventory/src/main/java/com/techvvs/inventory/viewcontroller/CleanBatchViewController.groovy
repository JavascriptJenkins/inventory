package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.VendorVO
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
import com.techvvs.inventory.labels.service.LabelPrintingService
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.util.Arrays


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
    VendorRepo vendorRepo;

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

    @Autowired
    LabelPrintingService labelPrintingService



    @GetMapping
    String viewEditForm(
            Model model,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("vendorid") Optional<Integer> vendorid

    ){

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, null, false , false, vendorid);
        return "batch/batch.html";
    }

    @GetMapping("/download-file")
    void downloadBatchFile(
            @RequestParam("batchid") String batchid,
            @RequestParam("directory") String directory,
            HttpServletResponse response
    ) {
        try {
            System.out.println("=== DOWNLOAD FILE DEBUG START ===")
            System.out.println("Received batchid: " + batchid)
            System.out.println("Received directory: " + directory)
            
            // Get batch information
            BatchVO batch = batchRepo.findById(Integer.valueOf(batchid)).orElse(null);
            if (batch == null) {
                System.out.println("ERROR: Batch not found for batchid: " + batchid)
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            System.out.println("Found batch: " + batch.batchnumber)
            System.out.println("Batch name: " + batch.name)

            // Construct file path - use the specific dymno28mmx89mm directory
            String baseDir = appConstants.PARENT_LEVEL_DIR + String.valueOf(batch.batchnumber) + appConstants.BARCODES_DYMNO_28mmx89mm_DIR
            String fullPath = baseDir
            
            System.out.println("PARENT_LEVEL_DIR: " + appConstants.PARENT_LEVEL_DIR)
            System.out.println("BARCODES_DYMNO_28mmx89mm_DIR: " + appConstants.BARCODES_DYMNO_28mmx89mm_DIR)
            System.out.println("Batch number: " + batch.batchnumber)
            System.out.println("Constructed baseDir: " + baseDir)
            System.out.println("Full path: " + fullPath)
            
            // Check if directory exists
            File dir = new File(fullPath)
            System.out.println("Directory exists: " + dir.exists())
            if (dir.exists()) {
                System.out.println("Directory is readable: " + dir.canRead())
                String[] filesInDir = dir.list()
                System.out.println("Files in directory: " + (filesInDir != null ? Arrays.toString(filesInDir) : "null"))
            }
            
            // Get files from the specified directory
            System.out.println("Calling techvvsFileHelper.getFilesByFileNumber with batchnumber: " + batch.batchnumber + " and path: " + fullPath)
            List<FileVO> files = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(batch.batchnumber), fullPath)
            
            System.out.println("Number of files found: " + files.size())
            for (int i = 0; i < files.size(); i++) {
                FileVO file = files.get(i)
                System.out.println("File " + i + ": " + file.getFilename() + " at directory: " + file.getDirectory())
            }
            
            if (files.isEmpty()) {
                System.out.println("ERROR: No files found in directory")
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Get the first file (assuming there's only one file in dymno28mmx89mm directory)
            FileVO fileToDownload = files.get(0)
            
            // Construct the full file path by combining directory and filename
            String fullFilePath = fileToDownload.getDirectory() + File.separator + fileToDownload.getFilename()
            File file = new File(fullFilePath)
            
            System.out.println("Attempting to download file: " + fileToDownload.getFilename())
            System.out.println("File directory: " + fileToDownload.getDirectory())
            System.out.println("File filename: " + fileToDownload.getFilename())
            System.out.println("Constructed full path: " + fullFilePath)
            System.out.println("File exists: " + file.exists())
            System.out.println("File is readable: " + file.canRead())
            System.out.println("File size: " + file.length())
            
            if (!file.exists()) {
                System.out.println("ERROR: File does not exist at path: " + fullFilePath)
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Set response headers for file download
            response.setContentType("application/octet-stream")
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileToDownload.getFilename() + "\"")
            response.setContentLength((int) file.length())
            
            System.out.println("Setting response headers:")
            System.out.println("Content-Type: application/octet-stream")
            System.out.println("Content-Disposition: attachment; filename=\"" + fileToDownload.getFilename() + "\"")
            System.out.println("Content-Length: " + file.length())

            // Stream the file to response
            FileInputStream fis = null
            OutputStream os = null
            try {
                fis = new FileInputStream(file)
                os = response.getOutputStream()
                
                byte[] buffer = new byte[4096]
                int bytesRead
                int totalBytesRead = 0
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                }
                os.flush()
                System.out.println("Successfully streamed " + totalBytesRead + " bytes to response")
            } finally {
                if (fis != null) {
                    fis.close()
                }
                if (os != null) {
                    os.close()
                }
            }
            
            System.out.println("=== DOWNLOAD FILE DEBUG END - SUCCESS ===")

        } catch (Exception e) {
            System.out.println("=== DOWNLOAD FILE DEBUG END - ERROR ===")
            System.out.println("Error downloading batch file: " + e.getMessage())
            e.printStackTrace()
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/generate-labels")
    @ResponseBody
    String generateLabels(
            @RequestParam("batchid") String batchid,
            HttpServletResponse response
    ) {
        try {
            // Get batch information
            BatchVO batch = batchRepo.findById(Integer.valueOf(batchid)).orElse(null);
            if (batch == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return "Batch not found";
            }

            // Generate labels using the same logic as the existing Generate Labels button
            System.out.println("Generating labels for batch: " + batch.batchnumber)
            
            // Call the same label generation services as the existing button
//            labelPrintingService.createEpsonC6000AuLabel4by6point5(batch)
            labelPrintingService.createDyno550TurboLabel28mmx89mm(batch)
            
            // Generate barcode manifest and all barcodes for the batch
//            batchControllerHelper.generateBarcodeManifestForBatch(String.valueOf(batch.batchnumber))
//            batchControllerHelper.generateAllBarcodesForBatch(String.valueOf(batch.batchnumber))
            
            // Generate default menu from batch
//            menuGenerator.generateDefaultMenuFromBatch(batch)
            
            System.out.println("Labels generated successfully for batch: " + batch.batchnumber)
            
            response.setStatus(HttpServletResponse.SC_OK);
            return "Labels generated successfully";

        } catch (Exception e) {
            System.out.println("Error generating labels: " + e.getMessage())
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "Error generating labels: " + e.getMessage();
        }
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

        techvvsAuthService.checkuserauth(model)
        bindViewDataForEditBatchPage(model, page, size)

        return "batch/managebatch.html";
    }


    // todo: enforce admin rights to view this page
    // Admin page for editing the batch, removing and adding products and whatnot
    @Transactional
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
//            product = productRepo.findByIdWithVendor(productid.get());
            product = productService.getProductWithLazyFieldsLoaded(productid.get());
            // 🚨 Force loading of vendorvo BEFORE returning to view
            if (product.get()?.vendorvo != null) {
                product.get()?.vendorvo?.name; // Access any property to initialize
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

        bindBatchTypes(model)
        bindProductTypes(model)
        bindBatches(model)
        bindVendors(model)
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
        bindVendors(model)
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


    void bindVendors(Model model){
        // get all the vendor objects and bind them to select dropdown
        List<VendorVO> vendorVOS = vendorRepo.findAll();
        model.addAttribute("vendors", vendorVOS);
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
    @PostMapping("/admin/move/product")
    String moveProduct(@ModelAttribute( "batch" ) BatchVO batchVO,
                 Model model,
                 @RequestParam("batchid") Optional<Integer> batchid,
                 @RequestParam("productnamesearch") Optional<String> productnamesearch,
                 @RequestParam Map<String, String> selectedProducts,
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
            model.addAttribute("carrierProduct", new ProductVO(batch: new BatchVO(batchid: batchid.get()))); // bind into blank carrier productVO
        }


        // we have to filter out any extra paramters that may have been passed in the form submission
        Map<String, String> filteredProducts = selectedProducts.findAll { key, value ->
            key.startsWith("selectedProducts-")
        }

        StringBuilder sb = new StringBuilder("Cannot move these product(s), as they exist in a transaction or in a cart -> \n")
        boolean hasProductsThatCannotBeMoved = false

        if(filteredProducts.size() > 0){


            List products = new ArrayList();
            for (Map.Entry<String, String> entry : filteredProducts.entrySet()) {
                Optional<ProductVO> productVO = productRepo.findById(Integer.valueOf(entry.getValue())) // find the product

                // first we need to run a check to make sure that none of these products have been sold / are in a transaction
                if(productVO.present &&
                        (productVO.get().transaction_list.size() == 0 || productVO.get().transaction_list == null) &&
                        (productVO.get().cart_list.size() == 0 || productVO.get().cart_list == null)

                ) {
                    productVO.present ? products.add(productVO.get()) : null // add product to the list if it exists
                } else {
                    // add the products that already exist in a transaction to a warning message so user knows which ones cannot be moved.
                    productVO.present ? sb.append(productVO.get().name + " : " + productVO.get().product_id + " | " + "\n") : null
                    hasProductsThatCannotBeMoved = true
                }

            }

            hasProductsThatCannotBeMoved ? model.addAttribute("warningMessage", sb.toString()) : null // add a warningMessage if we have products that cannot be moved

            Page<ProductVO> pageOfProduct = batchControllerHelper.getPageOfProducts(products, 0, products.size())

            model.addAttribute("pageNumbers", 0); // we will always be displaying the whole list of products being moved
            model.addAttribute("page", 0);
            model.addAttribute("size", pageOfProduct.getTotalPages());
            model.addAttribute("productPage", pageOfProduct);
            model.addAttribute("editmode", true);
        }
        // end binding the selected products into the next UI

        // start remove this code
//        Optional<String> filtertablename = Optional.empty()
//        // bind the product into scope
//        if(productid.isPresent() && productid.get() != 0 && productid.get() != null) {
//            product = productRepo.findById(productid.get());
//            filtertablename = Optional.of(product.get().name) // this will filter the table below
//            model.addAttribute("product", product.get());
//            model.addAttribute("editmode", true);
//        } else {
//            model.addAttribute("product", new ProductVO());
//            model.addAttribute("editmode", false);
//        }
        // end remove this code

//        // now go get the list of paginated products in the batch
//        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
//            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
//                    model,
//                    page,
//                    size,
//                    productnamesearch.get()
//            )
//        } else if(filtertablename.isPresent() && !filtertablename.isEmpty()) {
//            // default behavior is to bind a list of all the products in the batch to a table for display
//            batchControllerHelper.bindFilterProductsLikeSearchForMoveProductUI(model, page, size, filtertablename.get(), batch.get())
//            batchControllerHelper.getPageOfProducts()
//        } else {
//            // default behavior is to bind a list of all the products in the batch to a table for display
//            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
//        }

        bindBatches(model)
        bindVendors(model)
        bindBatchTypes(model)
        bindProductTypes(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/adminmoveproduct.html";
    }


    // todo: modify this to accept a list of productids
    // todo: make sure this cannot be done for products that already have started selling...
    // Admin page for moving products between batches
    @PostMapping("/admin/product/move")
    String moveProductPostMapping(@ModelAttribute( "batch" ) BatchVO batchVO,
                       Model model,
                       @RequestParam("batchid") Optional<Integer> batchid,
                       @RequestParam("productnamesearch") Optional<String> productnamesearch,
                       @RequestParam("productid") Optional<Integer> productid,
                       @ModelAttribute( "carrierProduct" ) ProductVO carrrierProductVO, // using this as a carrier object to bind the batchid from the ui
                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size,
                       @RequestParam Map<String, String> selectedProducts,
                       HttpServletRequest req){


        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()
        Optional<ProductVO> product = Optional.empty()

        StringBuilder sb = new StringBuilder("Moved these products to new batch: \n");

        Map<String, String> filteredProducts = selectedProducts.findAll { key, value ->
            key.startsWith("selectedProducts-")
        }
        if(filteredProducts.size() > 0) {
            List products = new ArrayList();
            for (Map.Entry<String, String> entry : filteredProducts.entrySet()) {
                Optional<ProductVO> productVO = productRepo.findById(Integer.valueOf(entry.getValue()))
                // find the product
//                productVO.present ? productsToMove.add(productVO.get()) : null

                if(batchid.present && productVO.present) {
                    int targetbatchid = carrrierProductVO.batch.batchid
                    carrrierProductVO.product_id = productVO.get().product_id // bind in the product id for the move logic
                    // here we have to parse the incoming productVO and see what the batchid is and move it
                    batch = Optional.of(batchService.moveProductToNewBatch(batchid.get(), carrrierProductVO, products, carrrierProductVO.batch.batchid))
                    sb.append(" | Product: "+productVO.get().name + " - " +productVO.get().product_id+ " moved successfully from batch: "+batchid.get() + " to batch: "+targetbatchid + " \n ")
                }

            }


            // bind the successMessage generated above
            model.addAttribute("successMessage", sb.toString())

            // bind the products that were moved into a data table
            Page<ProductVO> pageOfProduct = batchControllerHelper.getPageOfProducts(products, 0, products.size())
            model.addAttribute("pageNumbers", 0); // we will always be displaying the whole list of products being moved
            model.addAttribute("page", 0);
            model.addAttribute("size", pageOfProduct.getTotalPages());
            model.addAttribute("productPage", pageOfProduct);
            model.addAttribute("editmode", true);

        }





        // below is old code
//        if(batchid.present){
//            int targetbatchid = productVO.batch.batchid
//            // here we have to parse the incoming productVO and see what the batchid is and move it
//            batch = Optional.of(batchService.moveProductToNewBatch(batchid.get(), productVO))
//            model.addAttribute("successMessage", "Product moved successfully from batch: "+batchid.get() + " to batch: "+targetbatchid)
//        }
//
//        // bind the batch into scope
//        if(batchid.isPresent() && batch.isEmpty()){
//            batch = batchRepo.findById(batchid.get());
//            model.addAttribute("batch", batch.get());
//        }
//
//        // bind the product into scope
//        if(productid.isPresent() && productid.get() != 0 && productid.get() != null) {
//            product = productRepo.findById(productid.get());
//            model.addAttribute("product", product.get());
//            model.addAttribute("editmode", true);
//        } else {
//            model.addAttribute("product", new ProductVO());
//            model.addAttribute("editmode", false);
//        }
//
//        // now go get the list of paginated products in the batch
//        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
//            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
//                    model,
//                    page,
//                    size,
//                    productnamesearch.get()
//            )
//        } else {
//            // default behavior is to bind a list of all the products in the batch to a table for display
//            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
//        }

        bindBatches(model)
        bindVendors(model)
        bindBatchTypes(model)
        bindProductTypes(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/adminmoveproduct.html";
    }


    @GetMapping("/browseBatch")
    String browseBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size,
                             @RequestParam("vendorid") Optional<Integer> vendorid,
                             @RequestParam("producttypeid") Optional<Integer> producttypeid,
                             @RequestParam("batchtypeid") Optional<Integer> batchtypeid,
                             @RequestParam("filter") Optional<String> filter ){

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize, Sort.by(Sort.Direction.DESC, "batchid"));
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "batchid"));
        }

        // Apply filtering logic following TransactionHelper pattern
        Page<BatchVO> pageOfBatch = runPageRequest(pageable, vendorid, producttypeid, batchtypeid);

        int totalPages = pageOfBatch.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        // Add filter data to model
        model.addAttribute("vendors", vendorRepo.findAllByOrderByNameAsc());
        model.addAttribute("vendorid", vendorid.orElse(0));
        
        model.addAttribute("producttypes", productTypeRepo.findAll());
        model.addAttribute("producttypeid", producttypeid.orElse(0));
        
        model.addAttribute("batchtypes", batchTypeRepo.findAll());
        model.addAttribute("batchtypeid", batchtypeid.orElse(0));
        
        model.addAttribute("filter", filter.orElse(""));

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageSize);
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

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false, Optional.empty());
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

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false, Optional.empty());
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

        techvvsAuthService.checkuserauth(model)

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false, Optional.empty());
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

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, true, Optional.empty());
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

        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false, Optional.empty());

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


        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false, Optional.empty());

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


        model = batchControllerHelper.processModel(model,  batchid, editmode, page, productTypeVO, true, false, Optional.empty());

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


    @PostMapping ("/delete")
    String deleteBatch(
            @RequestParam("batchid") Integer batchid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            HttpServletRequest req){

        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        boolean result = batchService.deleteBatch(batchid);

        if(result){
            model.addAttribute("successMessage","Success deleting Batch with id: "+batchid+". All Products associated with this batch have also been deleted.  ");
            bindViewDataForEditBatchPage(model, page, size)

        } else {
            model.addAttribute("errorMessage","Error while deleting record. Check to see if any products in this batch exist in a cart or transaction.  ");
            bindViewDataForEditBatchPage(model, page, size)
        }
        return "batch/managebatch.html"

    }

    void bindViewDataForEditBatchPage(Model model, Optional<Integer> page, Optional<Integer> size){
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 10;
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
        model.addAttribute("batch", new BatchVO());
    }

    @Transactional
    Page<BatchVO> runPageRequest(
            Pageable pageable,
            Optional<Integer> vendorid,
            Optional<Integer> producttypeid,
            Optional<Integer> batchtypeid
    ) {
        // Convert Optional parameters to null/values following TransactionHelper pattern
        Integer vendorId = (vendorid.present && vendorid.get() > 0) ? vendorid.get() : null
        Integer productTypeId = (producttypeid.present && producttypeid.get() > 0) ? producttypeid.get() : null
        Integer batchTypeId = (batchtypeid.present && batchtypeid.get() > 0) ? batchtypeid.get() : null

        return batchRepo.findFilteredBatches(vendorId, productTypeId, batchTypeId, pageable)
    }

}
