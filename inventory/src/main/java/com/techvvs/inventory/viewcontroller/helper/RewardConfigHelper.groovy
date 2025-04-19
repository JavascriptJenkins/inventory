package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.RewardConfigRepo
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.parameters.P
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime
import java.util.stream.Collectors

@Component
class RewardConfigHelper {


    @Autowired
    RewardConfigRepo rewardConfigRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    @Autowired
    JwtTokenProvider jwtTokenProvider


    void loadBlankRewardConfig(Model model) {
        model.addAttribute("rewardconfig", new RewardConfigVO(rewardconfigid: 0))
    }

    RewardConfigVO validateRewardConfig(RewardConfigVO rewardconfig, Model model) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(rewardconfig, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(rewardconfig, model)

        // third - do any business logic / page specific validation below

        return rewardconfig
    }

    // only enforcing the name for now...
    RewardConfigVO createRewardConfig(RewardConfigVO rewardconfig) {

        rewardconfig.setIsactive(1)

        rewardconfig.createTimeStamp = LocalDateTime.now()
        rewardconfig.updateTimeStamp = LocalDateTime.now()
        rewardconfig = rewardConfigRepo.save(rewardconfig)
        return rewardconfig
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

        Page<RewardConfigVO> pageOfRewardConfig = rewardConfigRepo.findAll(pageable)

//        //filter out soft deleted rewardconfig records
//        pageOfRewardConfig.filter { it.deleted == 0 }

        int totalPages = pageOfRewardConfig.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while (totalPages > 0) {
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfRewardConfig.getTotalPages());
        model.addAttribute("rewardconfigPage", pageOfRewardConfig);
    }



    RewardConfigVO findRewardConfigById(Integer rewardconfigid) {
        Optional<RewardConfigVO> rewardconfigVO = rewardConfigRepo.findById(rewardconfigid)
        if (rewardconfigVO.isPresent()) {
            return rewardconfigVO.get()
        }
        return null
    }

    void deleteRewardConfig(Integer rewardconfigid, Model model) {
        RewardConfigVO rewardconfigVO = findRewardConfigById(rewardconfigid)
        if (rewardconfigVO != null) {
            rewardconfigVO.deleted = 1
            updateRewardConfig(rewardconfigVO, model)
            model.addAttribute(MessageConstants.SUCCESS_MSG, "RewardConfig deleted successfully!")
        } else {
            model.addAttribute(MessageConstants.ERROR_MSG, "RewardConfig failed to delete.")
        }
        loadBlankRewardConfig(model)
    }

    void getRewardConfig(Integer rewardconfigid, Model model) {
        RewardConfigVO rewardconfigVO = findRewardConfigById(rewardconfigid)
        if (rewardconfigVO != null) {
            model.addAttribute("rewardconfig", rewardconfigVO)
        } else {
            loadBlankRewardConfig(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "RewardConfig not found.")
        }
    }

    void updateRewardConfig(RewardConfigVO rewardconfigVO, Model model) {
        validateRewardConfig(rewardconfigVO, model)


        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {

            // check for existing rewards config with existing region
            rewardconfigVO  = checkForExistingConfigurationWithRegion(rewardconfigVO)


            if (rewardconfigVO.rewardconfigid > 0) {

                rewardconfigVO.updateTimeStamp = LocalDateTime.now()
                try {
                    rewardConfigRepo.save(rewardconfigVO)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "RewardConfig updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed")
                }
            } else {
                // If it's a new rewardconfig, use create method
                rewardconfigVO = createRewardConfig(rewardconfigVO)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "RewardConfig created successfully!")
            }
        }
        model.addAttribute("rewardconfig", rewardconfigVO)
    }

    // This ensures that if user submits an existing region, it will not be overwritten
    RewardConfigVO checkForExistingConfigurationWithRegion(RewardConfigVO rewardConfigVO){

        Optional<RewardConfigVO> existingRewardConfig = rewardConfigRepo.findByIsactiveAndRegion(1, rewardConfigVO.region)
        if(existingRewardConfig.isPresent()){
            // make sure the id is set, because user could have submitted it with id of 0 thinking they were going to create a new entry for same region.....
            rewardConfigVO.rewardconfigid = existingRewardConfig.get().rewardconfigid
            rewardConfigVO.isactive = existingRewardConfig.get().isactive
            rewardConfigVO.updateTimeStamp = existingRewardConfig.get().updateTimeStamp
            rewardConfigVO.createTimeStamp = existingRewardConfig.get().createTimeStamp
            return rewardConfigVO
        } else {
            rewardConfigVO
        }
    }

}
