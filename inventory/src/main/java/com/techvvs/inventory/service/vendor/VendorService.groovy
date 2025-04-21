package com.techvvs.inventory.service.vendor

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.model.VendorVO
import com.techvvs.inventory.model.VendorVO
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
class VendorService {

    @Autowired
    BatchRepo batchRepo

    @Autowired
    VendorRepo vendorRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    void logVendorToBatch(VendorVO vendorVO, int batchid) {

        // validate the vendorVO


        // fetch the batchvo from database based on batchid
        BatchVO batchVO = batchRepo.findById(batchid).get()

        // fill in missing vendorVO fields

        // save the vendorVO


    }


    VendorVO validateVendor(VendorVO vendorVO, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(vendorVO, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(vendorVO, model)

        // third - do any business logic / page specific validation below

        return vendorVO
    }


    void getVendor(Integer vendorid, Model model) {
        VendorVO vendorVO = findVendorById(vendorid)
        if (vendorVO != null) {
            model.addAttribute("vendor", vendorVO)
        } else {
            loadBlankVendor(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Vendor not found.")
        }
    }

    VendorVO findVendorById(Integer vendorid) {
        Optional<VendorVO> vendorVO = vendorRepo.findById(vendorid)
        if (vendorVO.isPresent()) {
            return vendorVO.get()
        }
        return null
    }


    void loadBlankVendor(Model model) {
        model.addAttribute("vendor", new VendorVO(vendorid: 0))
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

        Page<VendorVO> pageOfVendor = vendorRepo.findAll(pageable)

//        //filter out soft deleted vendor records
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
        model.addAttribute("vendorPage", pageOfVendor);
    }


    VendorVO createVendor(VendorVO vendor) {
        vendor.createTimeStamp = LocalDateTime.now()
        vendor.updateTimeStamp = LocalDateTime.now()
        vendor = vendorRepo.save(vendor)
        return vendor
    }


    void updateVendor(VendorVO vendorVO, Model model) {
        validateVendor(vendorVO, model)


        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {

            // check for existing rewards config with existing region
            vendorVO  = checkForExistingVendor(vendorVO)


            if (vendorVO.vendorid > 0) {

                vendorVO.updateTimeStamp = LocalDateTime.now()
                try {
                    vendorRepo.save(vendorVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Vendor updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new vendor, use create method
                vendorVO = createVendor(vendorVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Vendor created successfully!")
            }
        }
        model.addAttribute("vendor", vendorVO)
    }


    // This ensures that if user submits an existing region, it will not be overwritten
    VendorVO checkForExistingVendor(VendorVO vendorVO){

        Optional<VendorVO> existingVendor = vendorRepo.findById(vendorVO.vendorid)
        if(existingVendor.isPresent()){
            // make sure the id is set, because user could have submitted it with id of 0 thinking they were going to create a new entry for same region.....
            vendorVO.vendorid = existingVendor.get().vendorid
            vendorVO.expenses = existingVendor.get().expenses
            vendorVO.updateTimeStamp = existingVendor.get().updateTimeStamp
            vendorVO.createTimeStamp = existingVendor.get().createTimeStamp
            return vendorVO
        } else {
            vendorVO
        }
    }

}
