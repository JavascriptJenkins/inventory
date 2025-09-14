package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.LocationTypeRepo
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.location.LocationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@RequestMapping("/location")
@Controller
public class LocationViewController {

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    LocationService locationService

    @Autowired
    LocationTypeRepo locationTypeRepo

    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("locationid") Optional<Integer> locationid
    ){

        techvvsAuthService.checkuserauth(model)

        // attach a blank object to the model
        if(locationid.isPresent()){
            locationService.getLocation(locationid.get(), model)
        } else {
            locationService.loadBlankLocation(model)
        }

        locationService.addPaginatedData(model, page, size)

        // Add size parameter to model for template
        model.addAttribute("size", size.orElse(100))

        // load the values for dropdowns here
        bindStaticValues(model)

        return "admin/location.html";
    }

    void bindStaticValues(Model model) {
        model.addAttribute("locationtypes", locationTypeRepo.findAll())
    }

    @PostMapping("/create")
    String createLocation(
            @ModelAttribute( "location" ) LocationVO locationVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        techvvsAuthService.checkuserauth(model)

        locationVO = locationService.validateLocation(locationVO, model)

        // only proceed if there is no error
        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
            // create the location
            locationVO = locationService.createLocation(locationVO)
            model.addAttribute("successMessage", "Location created successfully!")
        }

        model.addAttribute("location", locationVO)
        locationService.addPaginatedData(model, page, size)
        
        // Add size parameter to model for template
        model.addAttribute("size", size.orElse(100))
        
        bindStaticValues(model)

        return "admin/location.html";
    }

    @PostMapping("/edit")
    String editLocation(
            @ModelAttribute( "location" ) LocationVO locationVO,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        locationService.updateLocation(locationVO, model)
        locationService.addPaginatedData(model, page, size)
        
        // Add size parameter to model for template
        model.addAttribute("size", size.orElse(100))
        
        bindStaticValues(model)
        return "admin/location.html"
    }

}
