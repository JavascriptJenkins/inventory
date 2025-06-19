package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.ProductDao
import com.techvvs.inventory.jparepo.AttributeRepo
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.AttributeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.service.attribute.constants.AttributeNameEnum
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateProduct
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse
import java.nio.file.Paths
import java.security.SecureRandom
import java.time.LocalDateTime

@RequestMapping("/product")
@Controller
public class ProductViewController {

    private final String UPLOAD_DIR = "./uploads/";
    

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    FilePagingService filePagingService

    @Autowired
    ControllerConstants controllerConstants

    @Autowired
    ProductRepo productRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;

    @Autowired
    VendorRepo vendorRepo;

    @Autowired
    AttributeRepo attributeRepo;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    ValidateProduct validateProduct;

    @Autowired
    ProductDao productDao
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductHelper productHelper

    @Autowired
    BarcodeGenerator barcodeGenerator


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "product" ) ProductVO productVO,
                       Model model,
                        @RequestParam("batchid") Optional<String> batchid
    ){
        techvvsAuthService.checkuserauth(model)



        ProductVO productVOToBind;
        if(productVO != null && productVO.getProduct_id() != null){
            productVOToBind = productVO;
        } else {
            productVOToBind = new ProductVO();
            if(batchid.isPresent()){
                productVOToBind.setBatch(batchRepo.findById(Integer.valueOf(batchid.get())).get())
                model.addAttribute("batchid", productVOToBind.batch.batchid);
            }
            productVOToBind.setProductnumber(0);
            productVOToBind.setProductnumber(secureRandom.nextInt(10000000));
            productVOToBind.producttypeid = new ProductTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        model.addAttribute("batch", new BatchVO()); // provide a default object for thymeleaf
        bindProductTypes(model)
        bindVendors(model)
        bindAttributes(model)
        bindBatchList(model, batchid) // bind list of batches so we can assign a new product to them?
        bindLastCreatedProduct(productVOToBind) // bind the vendor and product type of the last created product to the form
        model.addAttribute("product", productVOToBind);
        return "product/product.html";
    }



    void bindBatches(Model model, BatchVO batchVO){
        // get all the batch objects and bind them to select dropdown
       // List<BatchVO> batchVOS = batchRepo.findAll();
        model.addAttribute("batch", batchVO);
    }

    void bindBatchList(Model model, Optional<String> batchid){

        if(batchid.isPresent()){
            model.addAttribute("batchid", batchid.get());
        }


        List<BatchVO> batchVOS = batchRepo.findAll();
        model.addAttribute("batchlist", batchVOS);



    }


    void bindProductTypes(Model model){
        // get all the producttype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypes", productTypeVOS);
    }

    void bindVendors(Model model){
        // get all the vendor objects and bind them to select dropdown
        List<VendorVO> vendorVOS = vendorRepo.findAll();
        model.addAttribute("vendors", vendorVOS);
    }

    void bindAttributes(Model model){
        // get all the attribute objects and bind them to select dropdown
        List<AttributeVO> attributeVOS = attributeRepo.findAll();
        model.addAttribute("attributes", attributeVOS);
        model.addAttribute("attributenames", AttributeNameEnum.values()); // grab from the enum
    }

    void bindLastCreatedProduct(ProductVO productVOToBind){
        // do a query to find the last created product in the DB
        Optional<ProductVO> lastCreatedProduct = productRepo.findTopByOrderByCreateTimeStampDesc()

        if(lastCreatedProduct.isPresent()){
            productVOToBind.setProducttypeid(lastCreatedProduct.get().producttypeid)
            productVOToBind.setVendorvo(lastCreatedProduct.get()?.vendorvo)
        } else {
            // do nothing
        }

    }

    @GetMapping("/browseProduct")
    String browseProduct(@ModelAttribute( "product" ) ProductVO productVO,
                             Model model,
                             
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size ){

        techvvsAuthService.checkuserauth(model)


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

        Page<ProductVO> pageOfProduct = productRepo.findAll(pageable);

        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("product", new ProductVO());
        model.addAttribute("productPage", pageOfProduct);
        return "product/browseproduct.html";
    }

    @GetMapping("/searchProduct")
    String searchProduct(@ModelAttribute( "product" ) ProductVO productVO, Model model){



        techvvsAuthService.checkuserauth(model)
        model.addAttribute("product", new ProductVO());
        model.addAttribute("products", new ArrayList<ProductVO>(1));
        return "product/searchproduct.html";
    }

    @PostMapping("/searchProduct")
    String searchProductPost(@ModelAttribute( "product" ) ProductVO productVO, Model model){

        techvvsAuthService.checkuserauth(model)


        List<ProductVO> results = new ArrayList<ProductVO>();
        if(productVO.getProductnumber() != null){
            System.out.println("Searching data by getProductnumber");
            results = productRepo.findAllByProductnumber(productVO.getProductnumber());
        }
        if(productVO.getName() != null && results.size() == 0){
            System.out.println("Searching data by getName");
            results = productRepo.findAllByName(productVO.getName());
        }
        if(productVO.getDescription() != null && results.size() == 0){
            System.out.println("Searching data by getDescription");
            results = productRepo.findAllByDescription(productVO.getDescription());
        }

        model.addAttribute("batchid", productVO.batch.batchid);
        model.addAttribute("product", productVO);
        model.addAttribute("products", results);
        return "product/searchproduct.html";
    }

    //                        th:href="@{/product/editform?productid=__${product.productid}__&size=5&page=__${page}__&editmode=no&batchnumber=__${batchnumber}__&productnumber=__${productnumber}__}"
    @GetMapping("/editform")
    String viewEditForm(
                    Model model,

                    @RequestParam("batchid") Optional<Integer> batchid,
                    @RequestParam("editmode") String editmode,
                    @RequestParam("successMessage") Optional<String> successMessage,
                    @RequestParam("page") Optional<String> page,
                    @RequestParam("size") Optional<String> size,
                    @RequestParam("product_id") Optional<Integer> product_id

    ){
        techvvsAuthService.checkuserauth(model)


        if(batchid.isPresent()){
            model.addAttribute("batchid", batchid.get());
        }
        ProductVO productVO1 = new ProductVO(product_id: 0)
        if(product_id.isPresent()){
            System.out.println("Searching data by productnumber");
            productVO1 = productRepo.findById(product_id.get()).get()

        }

        productVO1.primaryphoto = appConstants.UPLOAD_DIR_IMAGES+productVO1.product_id


        // todo: rewrite this to get files by id
        // check to see if there are files uploaded related to this productnumber
        //List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(productnumber), UPLOAD_DIR);
        Page<FileVO> filePage = filePagingService.getFilePage(productVO1, Integer.valueOf(page.orElse("0")), Integer.valueOf(size.orElse("5")), Paths.get(appConstants.UPLOAD_DIR_MEDIA+appConstants.UPLOAD_DIR_PRODUCT).toString())

        if(filePage.size() > 0){
            model.addAttribute("filePage", filePage);
        } else {
            model.addAttribute("filePage", null);
        }

        if(successMessage.isPresent()){
            model.addAttribute("successMessage", successMessage.get());
        }
        model.addAttribute("batchid", productVO1.batch.batchid);
        model.addAttribute("editmode", editmode);
        model.addAttribute("product", productVO1);
        bindProductTypes(model)
        bindVendors(model)
        bindAttributes(model)
        bindBatches(model, productVO1.batch)
        return "product/editproduct.html";
    }

    // todo: edit this method so it only edits individual products and ignores batch entirely
    @PostMapping ("/editProduct")
    String editProduct(
            @ModelAttribute( "product" ) ProductVO productVO,
                                Model model
    ){

        techvvsAuthService.checkuserauth(model)

        String errorResult = validateProduct.validateNewFormInfo(productVO);


        ProductVO productresult = productVO


        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
            model.addAttribute("editmode","yes") // keep edit mode open if we catch an error
        } else {


            productresult = productDao.updateProduct(productVO)


            // check to see if there are files uploaded related to this productnumber
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(productVO.getProductnumber(), UPLOAD_DIR);
            if(filelist.size() > 0){
                model.addAttribute("filelist", filelist);
            } else {
                model.addAttribute("filelist", null);
            }

            model.addAttribute("successMessage","Record Successfully Saved.");

        }

        model.addAttribute("batchid", productVO.batch.batchid);
        model.addAttribute("editmode", "no");
        bindProductTypes(model)
        bindVendors(model)
        bindAttributes(model)
        return "product/editproduct.html";
    }

    @PostMapping ("/createNewProduct")
    String createNewProduct(@ModelAttribute( "product" ) ProductVO productVO,@ModelAttribute( "batch" ) BatchVO batchVO,
                                Model model,
                                HttpServletResponse response
    ){
        techvvsAuthService.checkuserauth(model)


        String errorResult = validateProduct.validateNewFormInfo(productVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            batchVO = batchRepo.findByBatchid(productVO.batch.getBatchid())
            productVO.setBatch(batchVO)

            // add the product to the database
            ProductVO result = saveNewProduct(model, productVO, batchVO)

            // add the product to the batch
            batchVO = addProductToBatch(batchVO,result)
//            model.addAttribute("hidebarcodemsg","true") // only hide the barcode message if product is added with success

        }

        model.addAttribute("batchid", productVO.batch.batchid);
        model.addAttribute("batch",batchVO) // bind the object back to the ui
        bindProductTypes(model)
        bindVendors(model)
        bindAttributes(model)
        bindBatchList(model, Optional.of(String.valueOf(batchVO.batchid))) // bind list of batches so we can assign a new product to them?

        //  bindBatches(model)
        return "product/product.html";
    }

    BatchVO addProductToBatch(BatchVO batchVO, ProductVO result){
        batchVO = batchRepo.findByBatchid(batchVO.getBatchid())
        if(batchVO.getBatchnumber()){

            batchVO.setUpdateTimeStamp(LocalDateTime.now());
            // this means a valid batch was found
            batchVO.getProduct_set().add(result) // add the product from database to the product set
            batchVO = batchRepo.save(batchVO)
            return batchVO

        } else {
            // no batch found in database, should never happen, throw an error here
            System.out.println("Batch not found in db when adding product - should never happen")
            return batchVO
        }

    }

    ProductVO saveNewProduct(Model model, ProductVO productVO, BatchVO batchVO) {


        productVO =  generateTimestampsAndBarcode(productVO, batchVO)

        // the second save saves the barcode i think .... this may be redundant
        ProductVO result = productRepo.save(productVO);

        model.addAttribute("successMessage","Record Successfully Saved. ");
        model.addAttribute("product", result);
        return result

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






}
