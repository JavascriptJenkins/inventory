package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import javax.servlet.http.HttpServletResponse
import java.time.LocalDateTime;


@RequestMapping("/batchtype")
@Controller
public class BatchTypeViewController {


    @Autowired BatchTypeRepo batchTypeRepo

    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "batchtype" ) BatchTypeVO batchTypeVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on batchtype controller: "+customJwtParameter);

        BatchTypeVO batchTypeVOToBind;
        if(batchTypeVO != null && batchTypeVO.getBatch_type_id() != null){
            batchTypeVOToBind = batchTypeVO;
        } else {
            batchTypeVOToBind = new BatchTypeVO();
            batchTypeVOToBind.batch_type_id= 0
        }

        model.addAttribute("batchtypelist", getBatchTypeList()); // todo: add pagination to this
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batchtype", batchTypeVOToBind);
        return "admin/batchtypes.html";
    }


    @PostMapping ("/editBatchType")
    String editBatchType(@ModelAttribute( "batchtype" ) BatchTypeVO batchTypeVO,
                     Model model,
                     HttpServletResponse response,
                     @RequestParam("customJwtParameter") String customJwtParameter
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateNewFormInfo(batchTypeVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            batchTypeVO.setUpdateTimeStamp(LocalDateTime.now());

            BatchTypeVO result = batchTypeRepo.save(batchTypeVO);

            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("batch", result);
        }

        model.addAttribute("batchtypelist", getBatchTypeList()); // todo: add pagination to this
        model.addAttribute("customJwtParameter", customJwtParameter);
        return "admin/batchtypes";
    }

    @PostMapping ("/createNewBatchType")
    String createNewBatchType(@ModelAttribute( "batchtype" ) BatchTypeVO batchTypeVO,
                          Model model,
                          HttpServletResponse response,
                          @RequestParam("customJwtParameter") String customJwtParameter
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateNewFormInfo(batchTypeVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            batchTypeVO.setCreateTimeStamp(LocalDateTime.now());
            batchTypeVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for batch types on the ui so we can save this batch object
            BatchTypeVO result = batchTypeRepo.save(batchTypeVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("batchtype", result);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batchtypelist", getBatchTypeList()); // todo: add pagination to this
        return "admin/batchtypes";
    }


    String validateNewFormInfo(BatchTypeVO batchTypeVO){

        if(batchTypeVO.getName() != null &&
                (batchTypeVO.getName().length() > 250
                        || batchTypeVO.getName().length() < 1)
        ){
            return "Name must be between 1-250 characters. ";
        }

        if(batchTypeVO.getDescription() != null && (batchTypeVO.getDescription().length() > 1000)
        ){
            return "Description must be less than 1000 characters";
        }

        return "success";
    }

    List<BatchTypeVO> getBatchTypeList(){
        batchTypeRepo.findAll()
    }



}
