package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.ProductTypeVO
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

@RequestMapping("/producttype")
@Controller
public class ProductTypeViewController {


    @Autowired ProductTypeRepo productTypeRepo

    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "producttype" ) ProductTypeVO productTypeVO,
                       Model model,
                       @RequestParam("customJwtParameter") String customJwtParameter,
                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size
    ){

        System.out.println("customJwtParam on producttype controller: "+customJwtParameter);

        ProductTypeVO productTypeVOToBind;
        if(productTypeVO != null && productTypeVO.getProduct_type_id() != null){
            productTypeVOToBind = productTypeVO;
        } else {
            productTypeVOToBind = new ProductTypeVO();
            productTypeVOToBind.product_type_id= 0
        }

       // model.addAttribute("producttypelist", getProductTypeList()); // todo: add pagination to this
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("producttype", productTypeVOToBind);
        addPaginatedData(model, page)
        return "admin/producttypes.html";
    }


    @PostMapping ("/editProductType")
    String editProductType(@ModelAttribute( "producttype" ) ProductTypeVO productTypeVO,
                     Model model,
                     HttpServletResponse response,
                     @RequestParam("customJwtParameter") String customJwtParameter,
    @RequestParam("page") Optional<Integer> page,
    @RequestParam("size") Optional<Integer> size){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateNewFormInfo(productTypeVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            productTypeVO.setUpdateTimeStamp(LocalDateTime.now());

            ProductTypeVO result = productTypeRepo.save(productTypeVO);

            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("product", result);
        }

    //    model.addAttribute("producttypelist", getProductTypeList()); // todo: add pagination to this
        model.addAttribute("customJwtParameter", customJwtParameter);
        addPaginatedData(model, page)
        return "admin/producttypes";
    }

    @PostMapping ("/createNewProductType")
    String createNewProductType(@ModelAttribute( "producttype" ) ProductTypeVO productTypeVO,
                          Model model,
                          HttpServletResponse response,
                          @RequestParam("customJwtParameter") String customJwtParameter,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateNewFormInfo(productTypeVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            productTypeVO.setCreateTimeStamp(LocalDateTime.now());
            productTypeVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for product types on the ui so we can save this product object
            ProductTypeVO result = productTypeRepo.save(productTypeVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("producttype", result);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("producttypelist", getProductTypeList()); // todo: add pagination to this
//        model.addAttribute("producttypelist", getProductTypeList()); // todo: add pagination to this
        addPaginatedData(model, page)
        return "admin/producttypes";
    }


    String validateNewFormInfo(ProductTypeVO productTypeVO){

        if(productTypeVO.getName() != null &&
                (productTypeVO.getName().length() > 250
                        || productTypeVO.getName().length() < 1)
        ){
            return "Name must be between 1-250 characters. ";
        }

        if(productTypeVO.getDescription() != null && (productTypeVO.getDescription().length() > 1000)
        ){
            return "Description must be less than 1000 characters";
        }

        return "success";
    }

    List<ProductTypeVO> getProductTypeList(){
        productTypeRepo.findAll()
    }

    void addPaginatedData(Model model, Optional<Integer> page){

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

        Page<ProductTypeVO> pageOfProductType = productTypeRepo.findAll(pageable);

        int totalPages = pageOfProductType.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProductType.getTotalPages());
        model.addAttribute("producttypePage", pageOfProductType);
        if(model.getAttribute("producttype") == null){
            model.addAttribute("producttype", new ProductTypeVO())
        }
    }



}
