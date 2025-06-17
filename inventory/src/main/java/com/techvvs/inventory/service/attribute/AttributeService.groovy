package com.techvvs.inventory.service.attribute

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.AttributeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.AttributeVO
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
class AttributeService {

    @Autowired
    BatchRepo batchRepo

    @Autowired
    AttributeRepo attributeRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    void logAttributeToBatch(AttributeVO attributeVO, int batchid) {

        // validate the attributeVO


        // fetch the batchvo from database based on batchid
        BatchVO batchVO = batchRepo.findById(batchid).get()

        // fill in missing attributeVO fields

        // save the attributeVO


    }


    AttributeVO validateAttribute(AttributeVO attributeVO, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(attributeVO, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(attributeVO, model)

        // third - do any business logic / page specific validation below

        return attributeVO
    }


    void getAttribute(Integer attributeid, Model model) {
        AttributeVO attributeVO = findAttributeById(attributeid)
        if (attributeVO != null) {
            model.addAttribute("attribute", attributeVO)
        } else {
            loadBlankAttribute(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Attribute not found.")
        }
    }

    AttributeVO findAttributeById(Integer attributeid) {
        Optional<AttributeVO> attributeVO = attributeRepo.findById(attributeid)
        if (attributeVO.isPresent()) {
            return attributeVO.get()
        }
        return null
    }


    void loadBlankAttribute(Model model) {
        model.addAttribute("attribute", new AttributeVO(attributeid: 0))
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

        Page<AttributeVO> pageOfAttribute = attributeRepo.findAll(pageable)

//        //filter out soft deleted attribute records
//        pageOfAttribute.filter { it.deleted == 0 }

        int totalPages = pageOfAttribute.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfAttribute.getTotalPages());
        model.addAttribute("attributePage", pageOfAttribute);
    }


    AttributeVO createAttribute(AttributeVO attribute) {
        attribute.createTimeStamp = LocalDateTime.now()
        attribute.updateTimeStamp = LocalDateTime.now()
        attribute = attributeRepo.save(attribute)
        return attribute
    }


    void updateAttribute(AttributeVO attributeVO, Model model) {
        validateAttribute(attributeVO, model)


        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {

            // check for existing rewards config with existing region
            attributeVO  = checkForExistingAttribute(attributeVO)


            if (attributeVO.attributeid > 0) {

                attributeVO.updateTimeStamp = LocalDateTime.now()
                try {
                    attributeRepo.save(attributeVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Attribute updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new attribute, use create method
                attributeVO = createAttribute(attributeVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Attribute created successfully!")
            }
        }
        model.addAttribute("attribute", attributeVO)
    }


    // This ensures that if user submits an existing region, it will not be overwritten
    AttributeVO checkForExistingAttribute(AttributeVO attributeVO){

        Optional<AttributeVO> existingAttribute = attributeRepo.findById(attributeVO.attributeid)
        if(existingAttribute.isPresent()){
            // make sure the id is set, because user could have submitted it with id of 0 thinking they were going to create a new entry for same region.....
            attributeVO.attributeid = existingAttribute.get().attributeid
            attributeVO.updateTimeStamp = existingAttribute.get().updateTimeStamp
            attributeVO.createTimeStamp = existingAttribute.get().createTimeStamp
            return attributeVO
        } else {
            attributeVO
        }
    }

}
