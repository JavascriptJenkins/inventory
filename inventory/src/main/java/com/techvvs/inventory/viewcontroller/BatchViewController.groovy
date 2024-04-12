package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.model.BatchTypeVO;
import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.modelnonpersist.FileVO;
import com.techvvs.inventory.util.TechvvsFileHelper;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@RequestMapping("/newbatch")
@Controller
public class BatchViewController {

    private final String UPLOAD_DIR = "./uploads/";

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    BatchTypeRepo batchTypeRepo;


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "batch" ) BatchVO batchVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        BatchVO batchVOToBind;
        if(batchVO != null && batchVO.getBatch_id() != null){
            batchVOToBind = batchVO;
        } else {
            batchVOToBind = new BatchVO();
            batchVOToBind.setBatchnumber(0);
            batchVOToBind.setBatchnumber(secureRandom.nextInt(10000000));
            batchVOToBind.batch_type_id = new BatchTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", batchVOToBind);
        bindBatchTypes(model)
        return "service/batch.html";
    }

    void bindBatchTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<BatchTypeVO> batchTypeVOS = batchTypeRepo.findAll();
        model.addAttribute("batchtypes", batchTypeVOS);
    }

    @GetMapping("/browseBatch")
    String browseBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             @RequestParam("customJwtParameter") String customJwtParameter,
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size ){

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

        Page<BatchVO> pageOfBatch = batchRepo.findAll(pageable);

        int totalPages = pageOfBatch.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfBatch.getTotalPages());
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", new BatchVO());
        model.addAttribute("batchPage", pageOfBatch);
        return "service/browsebatch.html";
    }

    @GetMapping("/searchBatch")
    String searchBatch(@ModelAttribute( "batch" ) BatchVO batchVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", new BatchVO());
        model.addAttribute("batchs", new ArrayList<BatchVO>(1));
        return "service/searchbatch.html";
    }

    @PostMapping("/searchBatch")
    String searchBatchPost(@ModelAttribute( "batch" ) BatchVO batchVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchVO.getBatchnumber() != null){
            System.out.println("Searching data by getBatchnumber");
            results = batchRepo.findAllByBatchnumber(batchVO.getBatchnumber());
        }
        if(batchVO.getName() != null && results.size() == 0){
            System.out.println("Searching data by getName");
            results = batchRepo.findAllByName(batchVO.getName());
        }
        if(batchVO.getDescription() != null && results.size() == 0){
            System.out.println("Searching data by getDescription");
            results = batchRepo.findAllByDescription(batchVO.getDescription());
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", batchVO);
        model.addAttribute("batchs", results);
        return "service/searchbatch.html";
    }

    @GetMapping("/editform")
    String viewEditForm(
                    Model model,
                    @RequestParam("customJwtParameter") String customJwtParameter,
                    @RequestParam("batchnumber") String batchnumber){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        // check to see if there are files uploaded related to this batchnumber
        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(batchnumber), UPLOAD_DIR);

        if(filelist.size() > 0){
            model.addAttribute("filelist", filelist);
        } else {
            model.addAttribute("filelist", null);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", results.get(0));
        bindBatchTypes(model)
        return "service/batch.html";
    }

    @PostMapping ("/editBatch")
    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
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

        String errorResult = validateNewFormInfo(batchVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            batchVO.setUpdateTimeStamp(LocalDateTime.now());



            BatchVO result = batchRepo.save(batchVO);

            // check to see if there are files uploaded related to this batchnumber
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), UPLOAD_DIR);
            if(filelist.size() > 0){
                model.addAttribute("filelist", filelist);
            } else {
                model.addAttribute("filelist", null);
            }

            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("batch", result);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        bindBatchTypes(model)
        return "service/batch.html";
    }

    @PostMapping ("/createNewBatch")
    String createNewBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
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

        String errorResult = validateNewFormInfo(batchVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
            model.addAttribute("errorMessage",errorResult);
        } else {

            // when creating a new processData entry, set the last attempt visit to now - this may change in future
            batchVO.setCreateTimeStamp(LocalDateTime.now());
            batchVO.setUpdateTimeStamp(LocalDateTime.now());


            //todo: add support for batch types on the ui so we can save this batch object
            BatchVO result = batchRepo.save(batchVO);

            model.addAttribute("successMessage","Record Successfully Saved. ");
            model.addAttribute("batch", result);
        }

        model.addAttribute("customJwtParameter", customJwtParameter);
        bindBatchTypes(model)
        return "service/batch.html";
    }

    String validateNewFormInfo(BatchVO batchVO){

        if(batchVO.getName() != null &&
                (batchVO.getName().length() > 250
                || batchVO.getName().length() < 1)
        ){
            return "first name must be between 1-250 characters. ";
        }



        if(batchVO.getNotes() != null && (batchVO.getNotes().length() > 1000)
        ){
            return "Notes must be less than 1000 characters";
        }

        return "success";
    }


}
