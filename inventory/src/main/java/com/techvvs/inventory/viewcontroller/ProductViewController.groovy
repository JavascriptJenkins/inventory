package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.dao.ProductDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateProduct
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

@RequestMapping("/product")
@Controller
public class ProductViewController {

    private final String UPLOAD_DIR = "inventory/uploads/";
    

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    ValidateProduct validateProduct;

    @Autowired
    ProductDao productDao
    
    @Autowired
    TechvvsAuthService techvvsAuthService


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "product" ) ProductVO productVO, Model model){



        ProductVO productVOToBind;
        if(productVO != null && productVO.getProduct_id() != null){
            productVOToBind = productVO;
        } else {
            productVOToBind = new ProductVO();
            productVOToBind.setProductnumber(0);
            productVOToBind.setProductnumber(secureRandom.nextInt(10000000));
            productVOToBind.producttypeid = new ProductTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("product", productVOToBind);
        model.addAttribute("batch", new BatchVO()); // provide a default object for thymeleaf
        bindProductTypes(model)
        bindBatchList(model) // bind list of batches so we can assign a new product to them?
        return "product/product.html";
    }



    void bindBatches(Model model, BatchVO batchVO){
        // get all the batch objects and bind them to select dropdown
       // List<BatchVO> batchVOS = batchRepo.findAll();
        model.addAttribute("batch", batchVO);
    }

    void bindBatchList(Model model){
         List<BatchVO> batchVOS = batchRepo.findAll();
        model.addAttribute("batchlist", batchVOS);

    }


    void bindProductTypes(Model model){
        // get all the producttype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypes", productTypeVOS);
    }

    @GetMapping("/browseProduct")
    String browseProduct(@ModelAttribute( "product" ) ProductVO productVO,
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
        techvvsAuthService.checkuserauth(model)
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

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("product", productVO);
        model.addAttribute("products", results);
        return "product/searchproduct.html";
    }

    @GetMapping("/editform")
    String viewEditForm(
                    Model model,

                    @RequestParam("batchnumber") Optional<String> batchnumber,
                    @RequestParam("editmode") String editmode,
                    @RequestParam("productnumber") String productnumber,
                    @RequestParam("successMessage") Optional<String> successMessage


    ){

        if(batchnumber.isPresent()){
            model.addAttribute("batchnumber", batchnumber.get());
        }

        List<ProductVO> results = new ArrayList<ProductVO>();
        if(productnumber != null){
            System.out.println("Searching data by productnumber");
            results = productRepo.findAllByProductnumber(Integer.valueOf(productnumber));
        }

        // check to see if there are files uploaded related to this productnumber
        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(productnumber), UPLOAD_DIR);

        if(filelist.size() > 0){
            model.addAttribute("filelist", filelist);
        } else {
            model.addAttribute("filelist", null);
        }

        if(successMessage.isPresent()){
            model.addAttribute("successMessage", successMessage.get());
        }
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("product", results.get(0));
        model.addAttribute("editmode", editmode);
        bindProductTypes(model)
        bindBatches(model, results.get(0).batch)
        return "product/editproduct.html";
    }

    // todo: edit this method so it only edits individual products and ignores batch entirely
    @PostMapping ("/editProduct")
    String editProduct(
            @ModelAttribute( "product" ) ProductVO productVO,
                                Model model
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

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

        model.addAttribute("product", productresult);
        model.addAttribute("editmode", "no");
        techvvsAuthService.checkuserauth(model)
        bindProductTypes(model)
        return "product/editproduct.html";
    }

    @PostMapping ("/createNewProduct")
    String createNewProduct(@ModelAttribute( "product" ) ProductVO productVO,@ModelAttribute( "batch" ) BatchVO batchVO,
                                Model model,
                                HttpServletResponse response
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateProduct.validateNewFormInfo(productVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            productVO.setBatch(batchVO)

            // add the product to the database
            ProductVO result = saveNewProduct(model, productVO)

            // add the product to the batch
            batchVO = addProductToBatch(batchVO,result)
//            model.addAttribute("hidebarcodemsg","true") // only hide the barcode message if product is added with success

        }

        model.addAttribute("batch",batchVO) // bind the object back to the ui
        techvvsAuthService.checkuserauth(model)
        bindProductTypes(model)
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

    ProductVO saveNewProduct(Model model, ProductVO productVO) {

        // when creating a new processData entry, set the last attempt visit to now - this may change in future
        productVO.setCreateTimeStamp(LocalDateTime.now());
        productVO.setUpdateTimeStamp(LocalDateTime.now());

        //todo: add support for product types on the ui so we can save this product object
        ProductVO result = productRepo.save(productVO);

        model.addAttribute("successMessage","Record Successfully Saved. ");
        model.addAttribute("product", result);
        return result

    }






}
