package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.validation.ValidateLocation
import com.techvvs.inventory.validation.ValidateLocation
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

@RequestMapping("/location")
@Controller
public class LocationViewController {


//    @Autowired LocationRepo locationRepo
    @Autowired LocationRepo locationRepo
    @Autowired ValidateLocation validateLocation
    @Autowired TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "location" ) LocationVO locationVO,
                       Model model,

                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size
    ){



        LocationVO locationVOToBind;
        if(locationVO != null && locationVO.locationid != null){
            locationVOToBind = locationVO;
        } else {
            locationVOToBind = new LocationVO();
            locationVOToBind.locationid= 0
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("location", locationVOToBind);
        addPaginatedData(model, page)
        return "admin/location.html";
    }


    @PostMapping ("/editLocation")
    String editLocation(@ModelAttribute( "location" ) LocationVO locationVO,
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

        String errorResult = validateLocation.validateNewFormInfo(locationVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            locationVO.setUpdateTimeStamp(LocalDateTime.now());

            LocationVO result = locationRepo.save(locationVO);

            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("package", result);
        }

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/location";
    }

    @PostMapping ("/createNewLocation")
    String createNewLocation(@ModelAttribute( "location" ) LocationVO locationVO,
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

        String errorResult = validateLocation.validateNewFormInfo(locationVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            locationVO.setCreateTimeStamp(LocalDateTime.now());
            locationVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for package types on the ui so we can save this package object
            LocationVO result = locationRepo.save(locationVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("location", result);
        }

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/location";
    }




    List<LocationVO> getLocationList(){
        locationRepo.findAll()
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

        Page<LocationVO> pageOfLocation = locationRepo.findAll(pageable);

        int totalPages = pageOfLocation.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfLocation.getTotalPages());
        model.addAttribute("locationPage", pageOfLocation);
        if(model.getAttribute("location") == null){
            model.addAttribute("location", new LocationVO())
        }
    }



}
