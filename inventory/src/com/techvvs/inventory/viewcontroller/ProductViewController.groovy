package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.util.TechvvsFileHelper
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

    private final String UPLOAD_DIR = "./uploads/";

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "product" ) ProductVO productVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on product controller: "+customJwtParameter);

        ProductVO productVOToBind;
        if(productVO != null && productVO.getProduct_id() != null){
            productVOToBind = productVO;
        } else {
            productVOToBind = new ProductVO();
            productVOToBind.setProductnumber(0);
            productVOToBind.setProductnumber(secureRandom.nextInt(10000000));
            productVOToBind.product_type_id = new ProductTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("product", productVOToBind);
        bindProductTypes(model)
        return "product/product.html";
    }

    void bindProductTypes(Model model){
        // get all the producttype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypes", productTypeVOS);
    }

    @GetMapping("/browseProduct")
    String browseProduct(@ModelAttribute( "product" ) ProductVO productVO,
                             Model model,
                             @RequestParam("customJwtParameter") String customJwtParameter,
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
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("product", new ProductVO());
        model.addAttribute("productPage", pageOfProduct);
        return "product/browseproduct.html";
    }

    @GetMapping("/searchProduct")
    String searchProduct(@ModelAttribute( "product" ) ProductVO productVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on product controller: "+customJwtParameter);

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("product", new ProductVO());
        model.addAttribute("products", new ArrayList<ProductVO>(1));
        return "product/searchproduct.html";
    }

    @PostMapping("/searchProduct")
    String searchProductPost(@ModelAttribute( "product" ) ProductVO productVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on product controller: "+customJwtParameter);

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

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("product", productVO);
        model.addAttribute("products", results);
        return "product/searchproduct.html";
    }

    @GetMapping("/editform")
    String viewEditForm(
                    Model model,
                    @RequestParam("customJwtParameter") String customJwtParameter,
                    @RequestParam("productnumber") String productnumber){

        System.out.println("customJwtParam on product controller: "+customJwtParameter);

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

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("product", results.get(0));
        bindProductTypes(model)
        return "product/product.html";
    }

    @PostMapping ("/editProduct")
    String editProduct(@ModelAttribute( "product" ) ProductVO productVO,
                                Model model,
                                HttpServletResponse response,
                                @RequestParam("customJwtParameter") String customJwtParameter
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateNewFormInfo(productVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            productVO.setUpdateTimeStamp(LocalDateTime.now());



            ProductVO result = productRepo.save(productVO);

            // check to see if there are files uploaded related to this productnumber
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(productVO.getProductnumber(), UPLOAD_DIR);
            if(filelist.size() > 0){
                model.addAttribute("filelist", filelist);
            } else {
                model.addAttribute("filelist", null);
            }

            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("product", result);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        bindProductTypes(model)
        return "product/product.html";
    }

    @PostMapping ("/createNewProduct")
    String createNewProduct(@ModelAttribute( "product" ) ProductVO productVO,
                                Model model,
                                HttpServletResponse response,
                                @RequestParam("customJwtParameter") String customJwtParameter
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateNewFormInfo(productVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            productVO.setCreateTimeStamp(LocalDateTime.now());
            productVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for product types on the ui so we can save this product object
            ProductVO result = productRepo.save(productVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("product", result);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        bindProductTypes(model)
        return "product/product.html";
    }

    String validateNewFormInfo(ProductVO productVO){

        if(productVO.getName() != null &&
                (productVO.getName().length() > 250
                || productVO.getName().length() < 1)
        ){
            return "first name must be between 1-250 characters. ";
        }



        if(productVO.getNotes() != null && (productVO.getNotes().length() > 1000)
        ){
            return "Notes must be less than 1000 characters";
        }

        return "success";
    }


}
