package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.metrc.global.MetrcGlobal
import com.techvvs.inventory.service.vendor.VendorService
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import com.techvvs.inventory.service.metrc.model.entity.MetrcFacilityVO
import com.techvvs.inventory.service.metrc.model.entity.PagedEmployeesVO
import com.techvvs.inventory.service.metrc.model.entity.LocationVO
import com.techvvs.inventory.service.metrc.model.dto.LocationDto
import java.util.List
import java.util.Optional
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationsVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationTypesVO

@RequestMapping("/metrc/admin")
@Controller
public class MetrcAdminViewController {
    

    @Autowired
    RewardConfigHelper rewardConfigHelper

    @Autowired
    MetrcGlobal metrcGlobal

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    VendorService vendorService

    @Autowired
    BatchRepo batchRepo

    @Autowired
    VendorRepo vendorRepo

    // show the default admin UI landing page for the METRC admin panel
    // Need ROLE_VIEW_METRC_RETAIL role in order to view this page
    @GetMapping
    String viewDefualtMetrcAdminLandingPage(
            Model model
    ){

        techvvsAuthService.checkuserauth(model)

        try {
            List<MetrcFacilityVO> facilities = metrcGlobal.getFacilities()
            model.addAttribute("facilities", facilities)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving facilities: " + e.getMessage())
        }

//        // attach a blank object to the model
//        if(vendorid.isPresent()){
//            vendorService.getVendor(vendorid.get(), model)
//        } else {
//            vendorService.loadBlankVendor(model)
//        }
//
//        vendorService.addPaginatedData(model, page, size)

        // load the values for dropdowns here
//        bindStaticValues(model)

        return "metrc/admin.html";
    }

    // show the facilities view page
    @GetMapping("/facilities")
    String viewFacilities(
            Model model
    ){
        techvvsAuthService.checkuserauth(model)

        try {
            List<MetrcFacilityVO> facilities = metrcGlobal.getFacilities()
            model.addAttribute("facilities", facilities)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving facilities: " + e.getMessage())
        }

        return "metrc/facility/getview.html";
    }

    // show the employees view page
    @GetMapping("/employees")
    String viewEmployees(
            Model model,
            @RequestParam("licenseNumber") Optional<String> licenseNumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        techvvsAuthService.checkuserauth(model)

        // Default values
        String license = licenseNumber.orElse("") // You might want to get this from a configuration or user session
        int currentPage = page.orElse(1)
        int pageSize = size.orElse(10)

        try {
            PagedEmployeesVO employees = metrcGlobal.getEmployees(license, currentPage, pageSize)
            model.addAttribute("employees", employees)
            model.addAttribute("currentPage", currentPage)
            model.addAttribute("pageSize", pageSize)
            model.addAttribute("licenseNumber", license)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving employees: " + e.getMessage())
        }

        return "metrc/employee/getview.html";
    }

    // show the active locations view page
    @GetMapping("/locations/active")
    String viewActiveLocations(
            Model model,
            @RequestParam("licenseNumber") Optional<String> licenseNumber
    ){
        techvvsAuthService.checkuserauth(model)

        String license = licenseNumber.orElse("")

        try {
            PagedLocationsVO locations = metrcGlobal.getActiveLocations(license)
            model.addAttribute("locations", locations)
            model.addAttribute("licenseNumber", license)
            model.addAttribute("locationType", "Active")
            
            // Fetch location types for the dropdown
            PagedLocationTypesVO locationTypes = metrcGlobal.getLocationTypes(license)
            model.addAttribute("locationTypes", locationTypes)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving active locations: " + e.getMessage())
        }

        return "metrc/location/getview.html";
    }

    // show the inactive locations view page
    @GetMapping("/locations/inactive")
    String viewInactiveLocations(
            Model model,
            @RequestParam("licenseNumber") Optional<String> licenseNumber
    ){
        techvvsAuthService.checkuserauth(model)

        String license = licenseNumber.orElse("")

        try {
            PagedLocationsVO locations = metrcGlobal.getInactiveLocations(license)
            model.addAttribute("locations", locations)
            model.addAttribute("licenseNumber", license)
            model.addAttribute("locationType", "Inactive")
            
            // Fetch location types for the dropdown
            PagedLocationTypesVO locationTypes = metrcGlobal.getLocationTypes(license)
            model.addAttribute("locationTypes", locationTypes)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving inactive locations: " + e.getMessage())
        }

        return "metrc/location/getview.html";
    }

    // create a new location
    @PostMapping("/locations/create")
    String createLocation(
            Model model,
            @ModelAttribute("location") LocationDto locationDto
    ){
        techvvsAuthService.checkuserauth(model)

        try {
            LocationVO createdLocation = metrcGlobal.createLocation(locationDto)
            model.addAttribute("successMessage", "Location created successfully")
            model.addAttribute("licenseNumber", locationDto.getLicenseNumber())
            
            // Redirect to active locations view
            PagedLocationsVO locations = metrcGlobal.getActiveLocations(locationDto.getLicenseNumber())
            model.addAttribute("locations", locations)
            model.addAttribute("locationType", "Active")
            
            // Fetch location types for the dropdown
            PagedLocationTypesVO locationTypes = metrcGlobal.getLocationTypes(locationDto.getLicenseNumber())
            model.addAttribute("locationTypes", locationTypes)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating location: " + e.getMessage())
        }

        return "metrc/location/getview.html";
    }

    // update an existing location
    @PostMapping("/locations/update")
    String updateLocation(
            Model model,
            @ModelAttribute("location") LocationDto locationDto
    ){
        techvvsAuthService.checkuserauth(model)

        try {
            LocationVO updatedLocation = metrcGlobal.updateLocation(locationDto)
            model.addAttribute("successMessage", "Location updated successfully")
            model.addAttribute("licenseNumber", locationDto.getLicenseNumber())
            
            // Redirect to active locations view
            PagedLocationsVO locations = metrcGlobal.getActiveLocations(locationDto.getLicenseNumber())
            model.addAttribute("locations", locations)
            model.addAttribute("locationType", "Active")
            
            // Fetch location types for the dropdown
            PagedLocationTypesVO locationTypes = metrcGlobal.getLocationTypes(locationDto.getLicenseNumber())
            model.addAttribute("locationTypes", locationTypes)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating location: " + e.getMessage())
        }

        return "metrc/location/getview.html";
    }

    // archive a location
    @PostMapping("/locations/archive")
    String archiveLocation(
            Model model,
            @RequestParam("locationId") Long locationId,
            @RequestParam("licenseNumber") String licenseNumber
    ){
        techvvsAuthService.checkuserauth(model)

        try {
            metrcGlobal.archiveLocation(locationId, licenseNumber)
            model.addAttribute("successMessage", "Location archived successfully")
            model.addAttribute("licenseNumber", licenseNumber)
            
            // Redirect to active locations view
            PagedLocationsVO locations = metrcGlobal.getActiveLocations(licenseNumber)
            model.addAttribute("locations", locations)
            model.addAttribute("locationType", "Active")
            
            // Fetch location types for the dropdown
            PagedLocationTypesVO locationTypes = metrcGlobal.getLocationTypes(licenseNumber)
            model.addAttribute("locationTypes", locationTypes)
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error archiving location: " + e.getMessage())
        }

        return "metrc/location/getview.html";
    }

    void bindStaticValues(Model model) {
//        model.addAttribute("paymentmethods", PaymentMethod.values());
//        model.addAttribute("expensetypes", ExpenseType.values());
//        model.addAttribute("batches", batchRepo.findAll())
//        model.addAttribute("vendors", vendorRepo.findAll())
    }


//    @PostMapping("/create")
//    String createVendor(
//            @ModelAttribute( "vendor" ) VendorVO vendorVO,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
//    ){
//        techvvsAuthService.checkuserauth(model)
//
//        vendorVO = vendorService.validateVendor(vendorVO, model)
//
//        // only proceed if there is no error
//        if(model.getAttribute(MessageConstants.ERROR_MSG) == null){
//            // create the customer
//            vendorVO = vendorService.createVendor(vendorVO)
//            model.addAttribute("successMessage", "Vendor created successfully!")
//        }
//
//        model.addAttribute("vendor", vendorVO)
//        vendorService.addPaginatedData(model, page, size)
//
//        return "vendor/admin.html";
//    }

    @PostMapping("/edit")
    String editVendor(
//            @ModelAttribute( "vendor" ) VendorVO vendorVO,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
    ) {
//        techvvsAuthService.checkuserauth(model)
//        vendorService.updateVendor(vendorVO, model)
//        vendorService.addPaginatedData(model, page, size)
        return "vendor/admin.html"
    }

    @GetMapping("/get")
    String getRewardConfig(
//            @RequestParam("customerid") Integer customerid,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
    ) {
//        rewardConfigHelper.getRewardConfig(customerid, model)
//        rewardConfigHelper.addPaginatedData(model, page, size)
//        techvvsAuthService.checkuserauth(model)
        return "customer/customer.html"
    }

    @PostMapping("/delete")
    String deleteRewardConfig(
//            @RequestParam("customerid") Integer customerid,
//            Model model,
//            @RequestParam("page") Optional<Integer> page,
//            @RequestParam("size") Optional<Integer> size
    ) {
//        rewardConfigHelper.deleteRewardConfig(customerid, model)
//        rewardConfigHelper.addPaginatedData(model, page, size)
//        techvvsAuthService.checkuserauth(model)
//        return "customer/customer.html"
    }
}
