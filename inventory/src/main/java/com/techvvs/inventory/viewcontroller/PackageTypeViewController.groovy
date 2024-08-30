package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.validation.ValidatePackageType
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

@RequestMapping("/packagetype")
@Controller
public class PackageTypeViewController {


    @Autowired PackageTypeRepo packageTypeRepo
    @Autowired ValidatePackageType validatePackageType
    @Autowired TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "packagetype" ) PackageTypeVO packageTypeVO,
                       Model model,

                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size
    ){



        PackageTypeVO packageTypeVOToBind;
        if(packageTypeVO != null && packageTypeVO.packagetypeid != null){
            packageTypeVOToBind = packageTypeVO;
        } else {
            packageTypeVOToBind = new PackageTypeVO();
            packageTypeVOToBind.packagetypeid= 0
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("packagetype", packageTypeVOToBind);
        addPaginatedData(model, page)
        return "admin/packagetypes.html";
    }


    @PostMapping ("/editPackageType")
    String editPackageType(@ModelAttribute( "packagetype" ) PackageTypeVO packageTypeVO,
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

        String errorResult = validatePackageType.validateNewFormInfo(packageTypeVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            packageTypeVO.setUpdateTimeStamp(LocalDateTime.now());

            PackageTypeVO result = packageTypeRepo.save(packageTypeVO);

            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("package", result);
        }

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/packagetypes";
    }

    @PostMapping ("/createNewPackageType")
    String createNewPackageType(@ModelAttribute( "packagetype" ) PackageTypeVO packageTypeVO,
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

        String errorResult = validatePackageType.validateNewFormInfo(packageTypeVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            packageTypeVO.setCreateTimeStamp(LocalDateTime.now());
            packageTypeVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for package types on the ui so we can save this package object
            PackageTypeVO result = packageTypeRepo.save(packageTypeVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("packagetype", result);
        }

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/packagetypes";
    }




    List<PackageTypeVO> getPackageTypeList(){
        packageTypeRepo.findAll()
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

        Page<PackageTypeVO> pageOfPackageType = packageTypeRepo.findAll(pageable);

        int totalPages = pageOfPackageType.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfPackageType.getTotalPages());
        model.addAttribute("packagetypePage", pageOfPackageType);
        if(model.getAttribute("packagetype") == null){
            model.addAttribute("packagetype", new PackageTypeVO())
        }
    }



}
