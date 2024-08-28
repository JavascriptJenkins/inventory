package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.validation.ValidateBatchType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
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
    @Autowired ValidateBatchType validateBatchType
    @Autowired TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "batchtype" ) BatchTypeVO batchTypeVO,
                       Model model,
                       
                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size
    ){

        

        BatchTypeVO batchTypeVOToBind;
        if(batchTypeVO != null && batchTypeVO.getBatch_type_id() != null){
            batchTypeVOToBind = batchTypeVO;
        } else {
            batchTypeVOToBind = new BatchTypeVO();
            batchTypeVOToBind.batch_type_id= 0
        }

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batchtype", batchTypeVOToBind);
        addPaginatedData(model, page)
        return "admin/batchtypes.html";
    }


    @PostMapping ("/editBatchType")
    String editBatchType(@ModelAttribute( "batchtype" ) BatchTypeVO batchTypeVO,
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

        String errorResult = validateBatchType.validateNewFormInfo(batchTypeVO);

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

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/batchtypes";
    }

    @PostMapping ("/createNewBatchType")
    String createNewBatchType(@ModelAttribute( "batchtype" ) BatchTypeVO batchTypeVO,
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

        String errorResult = validateBatchType.validateNewFormInfo(batchTypeVO);

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

        techvvsAuthService.checkuserauth(model)
        addPaginatedData(model, page)
        return "admin/batchtypes";
    }




    List<BatchTypeVO> getBatchTypeList(){
        batchTypeRepo.findAll()
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

        Page<BatchTypeVO> pageOfBatchType = batchTypeRepo.findAll(pageable);

        int totalPages = pageOfBatchType.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfBatchType.getTotalPages());
        model.addAttribute("batchtypePage", pageOfBatchType);
        if(model.getAttribute("batchtype") == null){
            model.addAttribute("batchtype", new BatchTypeVO())
        }
    }



}
