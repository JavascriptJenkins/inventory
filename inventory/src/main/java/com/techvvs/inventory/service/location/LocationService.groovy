package com.techvvs.inventory.service.location

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.LocationTypeRepo
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import java.time.LocalDateTime

@Service
class LocationService {

    @Autowired
    LocationRepo locationRepo

    @Autowired
    LocationTypeRepo locationTypeRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    LocationVO validateLocation(LocationVO locationVO, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(locationVO, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(locationVO, model)

        // third - do any business logic / page specific validation below

        return locationVO
    }

    void getLocation(Integer locationid, Model model) {
        LocationVO locationVO = findLocationById(locationid)
        if (locationVO != null) {
            model.addAttribute("location", locationVO)
        } else {
            loadBlankLocation(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Location not found.")
        }
    }

    LocationVO findLocationById(Integer locationid) {
        Optional<LocationVO> locationVO = locationRepo.findById(locationid)
        if (locationVO.isPresent()) {
            return locationVO.get()
        }
        return null
    }

    void loadBlankLocation(Model model) {
        model.addAttribute("location", new LocationVO(locationid: 0))
    }

    void addPaginatedData(Model model, Optional<Integer> page, Optional<Integer> size) {

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(100);
        Pageable pageable;
        if (currentPage == 0) {
            pageable = PageRequest.of(0, pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<LocationVO> pageOfLocation = locationRepo.findAll(pageable)

        int totalPages = pageOfLocation.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfLocation.getTotalPages());
        model.addAttribute("locationPage", pageOfLocation);
    }

    LocationVO createLocation(LocationVO location) {
        location.createTimeStamp = LocalDateTime.now()
        location.updateTimeStamp = LocalDateTime.now()
        location = locationRepo.save(location)
        return location
    }

    void updateLocation(LocationVO locationVO, Model model) {
        validateLocation(locationVO, model)

        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {

            // check for existing location
            locationVO = checkForExistingLocation(locationVO)

            if (locationVO.locationid > 0) {

                locationVO.updateTimeStamp = LocalDateTime.now()
                try {
                    locationRepo.save(locationVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Location updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new location, use create method
                locationVO = createLocation(locationVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Location created successfully!")
            }
        }
        model.addAttribute("location", locationVO)
    }

    // This ensures that if user submits an existing location, it will not be overwritten
    LocationVO checkForExistingLocation(LocationVO locationVO){

        Optional<LocationVO> existingLocation = locationRepo.findById(locationVO.locationid)
        if(existingLocation.isPresent()){
            // make sure the id is set, because user could have submitted it with id of 0 thinking they were going to create a new entry for same location.....
            locationVO.locationid = existingLocation.get().locationid
            locationVO.updateTimeStamp = existingLocation.get().updateTimeStamp
            locationVO.createTimeStamp = existingLocation.get().createTimeStamp
            return locationVO
        } else {
            locationVO
        }
    }

}
