package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.validation.ValidateProductType
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
    @Autowired ValidateProductType validateProductType
    @Autowired TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "producttype" ) ProductTypeVO productTypeVO,
                       Model model,

                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size
    ){



        ProductTypeVO productTypeVOToBind;
        if(productTypeVO != null && productTypeVO.getProducttypeid() != null){
            productTypeVOToBind = productTypeVO;
        } else {
            productTypeVOToBind = new ProductTypeVO();
            productTypeVOToBind.producttypeid= 0
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("producttype", productTypeVOToBind);
        addPaginatedData(model, page)
        return "admin/producttypes.html";
    }


    @PostMapping ("/editProductType")
    String editProductType(@ModelAttribute( "producttype" ) ProductTypeVO productTypeVO,
                     Model model,
                     HttpServletResponse response,
                     
    @RequestParam("page") Optional<Integer> page,
    @RequestParam("size") Optional<Integer> size){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateProductType.validateNewFormInfo(productTypeVO);

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

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/producttypes";
    }

    @PostMapping ("/createNewProductType")
    String createNewProductType(@ModelAttribute( "producttype" ) ProductTypeVO productTypeVO,
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

        String errorResult = validateProductType.validateNewFormInfo(productTypeVO);

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

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/producttypes";
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
