package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao;
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO;
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.modelnonpersist.FileVO;
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper;
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
import javax.servlet.http.HttpServletResponse
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@RequestMapping("/newbatch")
@Controller
public class BatchViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    BatchDao batchDao;


    @Autowired
    BatchTypeRepo batchTypeRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;


    @Autowired
    ProductRepo productRepo;


    @Autowired
    ValidateBatch validateBatch;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    ControllerConstants controllerConstants


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(@ModelAttribute( "batch" ) BatchVO batchVO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        BatchVO batchVOToBind;
        if(batchVO != null && batchVO.getBatchid() != null){
            batchVOToBind = batchVO;
        } else {
            batchVOToBind = new BatchVO();
            batchVOToBind.setBatchnumber(0);
//            int batchNumber = 100000 + secureRandom.nextInt(900000); // Generates a random number between 100000 and 999999
            int batchNumber = Integer.valueOf(batchControllerHelper.generateBatchNumber()); // Generates a random number between 100000 and 999999
            batchVOToBind.setBatchnumber(batchNumber);
            batchVOToBind.batch_type_id = new BatchTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", batchVOToBind);
        batchControllerHelper.bindBatchTypes(model)
        return "service/batch.html";
    }

    // uploadxlsx
    @GetMapping("/uploadxlsx")
    String viewUploadXslxNewBatch(
            @ModelAttribute( "batch" ) BatchVO batchVO,
                                  Model model,
            @RequestParam("customJwtParameter") String customJwtParameter

    ){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        BatchVO batchVOToBind;
        if(batchVO != null && batchVO.getBatchid() != null){
            batchVOToBind = batchVO;
        } else {
            batchVOToBind = new BatchVO();
            batchVOToBind.setBatchnumber(0);
            batchVOToBind.setBatchnumber(Integer.valueOf(batchControllerHelper.generateBatchNumber()));
//            batchVOToBind.setBatchnumber(secureRandom.nextInt(10000000));
            batchVOToBind.batch_type_id = new BatchTypeVO()
        }


        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", batchVOToBind);
        batchControllerHelper.bindBatchTypes(model)
        return "service/xlsxbatch.html";
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
        model.addAttribute("batchPage", pageOfBatch);
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", new BatchVO());
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

    // TODO: this needs to have the productPage stuff
    @GetMapping("/editform")
    String viewEditForm(
                    Model model,
                    @RequestParam("customJwtParameter") String customJwtParameter,
                    @RequestParam("editmode") String editmode,
                    @RequestParam("batchnumber") String batchnumber,
                    @RequestParam("page") Optional<Integer> page,
                    @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, null, false , false);
        return "service/editbatch.html";
    }

    @PostMapping("/filtereditform")
    String viewFilterEditForm(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, false);
        return "service/editbatch.html";
    }

    //    This will text the user a link to a menu pdf they can send people
    @PostMapping("/printmenu")
    String printmenu(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, false);
        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        batchControllerHelper.sendTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchnumber)

        return "service/editbatch.html";
    }

    @PostMapping("/likesearch")
    String viewLikeSearch(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, true);
        return "service/editbatch.html";
    }

    @PostMapping("/generatebarcodes")
    String viewGenerateBarcodes(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.ALL:
                batchControllerHelper.generateAllBarcodesForBatch(batchnumber)
                break
            case controllerConstants.SINGLE_MENU:
                batchControllerHelper.generateSingleMenuBarcodesForBatch(batchnumber)
                break
        }



        model.addAttribute("successMessage","Successfully created barcodes for this batch. ")

        return "service/editbatch.html";
    }


    @PostMapping("/generateqrcodes")
    String viewGenerateQrcodes(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        model = batchControllerHelper.processModel(model, customJwtParameter, batchnumber, editmode, page, productTypeVO, true, false);

        batchControllerHelper.generateQrcodesForBatch(batchnumber)

        model.addAttribute("successMessage","Successfully created qr codes for this batch. ")

        return "service/editbatch.html";
    }

    // todo: add the pagination crap here too
    @PostMapping ("/editBatch")
    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                     @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
                     Model model,
                                HttpServletResponse response,
                                @RequestParam("customJwtParameter") String customJwtParameter,
                     @RequestParam("page") Optional<Integer> page,
                     @RequestParam("size") Optional<Integer> size
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("----------------------- START AUTH INFO ");
        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
        System.out.println("----------------------- END AUTH INFO ");

        String errorResult = validateBatch.validateNewFormInfo(batchVO);

        // Validation
        if(!errorResult.equals("success")){
            model.addAttribute("errorMessage",errorResult);
            model.addAttribute("editmode","yes") //
        } else {

            BatchVO result = batchDao.updateBatch(batchVO);
           // BatchVO result = batchRepo.save(batchVO);

            // check to see if there are files uploaded related to this batchnumber
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), appConstants.UPLOAD_DIR);
            if(filelist.size() > 0){
                model.addAttribute("filelist", filelist);
            } else {
                model.addAttribute("filelist", null);
            }

            model.addAttribute("editmode","no") // after succesful edit is done we set this back to no
            model.addAttribute("successMessage","Record Successfully Saved.");
            model.addAttribute("batch", result);
        }

        batchControllerHelper.bindProducts(model, page, productTypeVO)
        batchControllerHelper.bindProductTypes(model)
        //  bindFilterProducts(model, page, productTypeVO)
        model.addAttribute("searchproducttype", new ProductTypeVO()) // this is a blank object for submitting a search term
        model.addAttribute("customJwtParameter", customJwtParameter);
        batchControllerHelper.bindBatchTypes(model)
        return "service/editbatch.html";
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

        String errorResult = validateBatch.validateNewFormInfo(batchVO);

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
        batchControllerHelper.bindBatchTypes(model)
        return "service/batch.html";
    }




}
