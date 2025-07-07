package com.techvvs.inventory.service.metrc.license

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.MetrcLicenseRepo
import com.techvvs.inventory.model.metrc.MetrcLicenseVO
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
class MetrcLicenseService {
    
    @Autowired
    MetrcLicenseRepo metrclicenseRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    void getMetrcLicense(Integer metrclicenseid, Model model) {
        MetrcLicenseVO metrclicenseVO = findMetrcLicenseById(metrclicenseid)
        if (metrclicenseVO != null) {
            model.addAttribute("metrclicense", metrclicenseVO)
        } else {
            loadBlankMetrcLicense(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "MetrcLicense not found.")
        }
    }

    MetrcLicenseVO findMetrcLicenseById(Integer metrclicenseid) {
        Optional<MetrcLicenseVO> metrclicenseVO = metrclicenseRepo.findById(metrclicenseid)
        if (metrclicenseVO.isPresent()) {
            return metrclicenseVO.get()
        }
        return null
    }

    void loadBlankMetrcLicense(Model model) {
        model.addAttribute("metrclicense", new MetrcLicenseVO(metrclicenseid: 0))
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

        Page<MetrcLicenseVO> pageOfVendor = metrclicenseRepo.findAll(pageable)

//        //filter out soft deleted metrclicense records
//        pageOfVendor.filter { it.deleted == 0 }

        int totalPages = pageOfVendor.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfVendor.getTotalPages());
        model.addAttribute("metrclicensePage", pageOfVendor);
    }


    MetrcLicenseVO validateMetrcLicense(MetrcLicenseVO metrclicenseVO, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(metrclicenseVO, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(metrclicenseVO, model)

        // third - do any business logic / page specific validation below

        return metrclicenseVO
    }


    MetrcLicenseVO createMetrcLicense(MetrcLicenseVO metrclicense) {
        metrclicense.createTimeStamp = LocalDateTime.now()
        metrclicense.updateTimeStamp = LocalDateTime.now()
        metrclicense = metrclicenseRepo.save(metrclicense)
        return metrclicense
    }


    void updateMetrcLicense(MetrcLicenseVO metrclicenseVO, Model model) {
        validateMetrcLicense(metrclicenseVO, model)


        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {

            // check for existing rewards config with existing region
            metrclicenseVO  = checkForExistingMetrcLicense(metrclicenseVO)


            if (metrclicenseVO.metrclicenseid > 0) {

                metrclicenseVO.updateTimeStamp = LocalDateTime.now()
                try {
                    metrclicenseRepo.save(metrclicenseVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "MetrcLicense updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new vendor, use create method
                metrclicenseVO = createMetrcLicense(metrclicenseVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Vendor created successfully!")
            }
        }
        model.addAttribute("metrclicense", metrclicenseVO)
    }


    // This ensures that if user submits an existing region, it will not be overwritten
    MetrcLicenseVO checkForExistingMetrcLicense(MetrcLicenseVO metrclicenseVO){

        Optional<MetrcLicenseVO> existingMetrcLicense = metrclicenseRepo.findById(metrclicenseVO.metrclicenseid)
        if(existingMetrcLicense.isPresent()){
            // make sure the id is set, because user could have submitted it with id of 0 thinking they were going to create a new entry for same region.....
            metrclicenseVO.metrclicenseid = existingMetrcLicense.get().metrclicenseid
            metrclicenseVO.updateTimeStamp = existingMetrcLicense.get().updateTimeStamp
            metrclicenseVO.createTimeStamp = existingMetrcLicense.get().createTimeStamp
            return metrclicenseVO
        } else {
            metrclicenseVO
        }
    }



}
