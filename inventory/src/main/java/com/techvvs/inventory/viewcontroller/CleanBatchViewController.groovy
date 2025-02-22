package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.time.LocalDateTime


/*refactoring the dirty batch controller into this one*/
@RequestMapping("/batch")
@Controller
public class CleanBatchViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    ProductRepo productRepo

    @Autowired
    BatchDao batchDao;

    @Autowired
    ValidateBatch validateBatch;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    FormattingUtil formattingUtil

    @Autowired
    ControllerConstants controllerConstants
    
    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    ProductTypeRepo productTypeRepo



    @GetMapping
    String viewEditForm(
            Model model,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, null, false , false);
        return "batch/batch.html";
    }

    // uploadxlsx
    @GetMapping("/uploadxlsx")
    String viewUploadXslxNewBatch(
            @ModelAttribute( "batch" ) BatchVO batchVO,
                                  Model model
    ){



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
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", batchVOToBind);
        batchControllerHelper.bindBatchTypes(model)
        return "batch/xlsxbatch.html";
    }


    @GetMapping("/edit")
    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                       Model model,

                       @RequestParam("page") Optional<Integer> page,
                       @RequestParam("size") Optional<Integer> size ){

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize, Sort.by(Sort.Direction.ASC, "batchid"));
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.ASC, "batchid"));
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
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        return "batch/managebatch.html";
    }


    // todo: enforce admin rights to view this page
    // Admin page for editing the batch, removing and adding products and whatnot
    @GetMapping("/admin")
    String admin(@ModelAttribute( "batch" ) BatchVO batchVO,
                 Model model,
                 @RequestParam("batchid") Optional<Integer> batchid,
                 @RequestParam("productnamesearch") Optional<String> productnamesearch,
                 @RequestParam("productid") Optional<Integer> productid,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size,
                 HttpServletRequest req){


        if(enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()
        Optional<ProductVO> product = Optional.empty()

        // bind the batch into scope
        if(batchid.isPresent()){
            batch = batchRepo.findById(batchid.get());
            model.addAttribute("batch", batch.get());
        }

        // bind the batch into scope
        if(productid.isPresent() && productid.get() != 0 && productid.get() != null) {
            product = productRepo.findById(productid.get());
            model.addAttribute("product", product.get());
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("product", new ProductVO());
            model.addAttribute("editmode", false);
        }

        // now go get the list of paginated products in the batch



        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
                    model,
                    page,
                    size,
                    productnamesearch.get()
            )
        } else {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
        }

        bindProductTypes(model)
        techvvsAuthService.checkuserauth(model)
        return "batch/admin.html";
    }

    void bindProductTypes(Model model){
        // get all the producttype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypes", productTypeVOS);
    }

    // todo: enforce admin rights to view this page
    // Admin page for editing the batch, removing and adding products and whatnot
    @GetMapping("/admin/selectproduct")
    String adminSelectProduct(@ModelAttribute( "batch" ) BatchVO batchVO,
                 Model model,
                 @RequestParam("batchid") Optional<Integer> batchid,
                 @RequestParam("productid") Optional<Integer> productid,
                 @RequestParam("productnamesearch") Optional<String> productnamesearch,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size,
                 HttpServletRequest req){


        if(enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // this batch object will be the batch in scope
        Optional<BatchVO> batch = Optional.empty()

        // bind the batch into scope
        if(batchid.isPresent()){
            batch = batchRepo.findById(batchid.get());
            model.addAttribute("batch", batch.get());
        }

        // now go get the list of paginated products in the batch



        if(productnamesearch.isPresent() && productnamesearch.get() != null && productnamesearch.get() != "" && productnamesearch.get() != "null") {
            batchControllerHelper.bindFilterProductsLikeSearchForCheckoutUI(
                    model,
                    page,
                    size,
                    productnamesearch.get()
            )
        } else {
            // default behavior is to bind a list of all the products in the batch to a table for display
            batchControllerHelper.bindAllProducts(model, page, size, batch.get())
        }

        techvvsAuthService.checkuserauth(model)
        return "batch/admin.html";
    }


    boolean enforceAdminRights(Model model, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        String token = ""
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("techvvs_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        } else {
            return false // return false if cookies is null
        }

        List<String> authorities = jwtTokenProvider.extractAuthorities(token) // passing internal token in here
        if(hasRole(authorities, String.valueOf(Role.ROLE_ADMIN))){
            // write code here to add certain attributes to the model that will enable the user to click certain buttons
            model.addAttribute("AdminViewActivated", "yes")
            return true
        }
        return false
    }

    boolean hasRole(List authorities, String roleToCheck) {
        println "Authorities: ${authorities}"
        println "Role to check: ${roleToCheck}"

        return authorities.any { authority ->
            def valueToCompare = authority instanceof GrantedAuthority ? authority.authority : authority.toString()
            valueToCompare == roleToCheck
        }
    }

    @GetMapping("/browseBatch")
    String browseBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             
                             @RequestParam("page") Optional<Integer> page,
                             @RequestParam("size") Optional<Integer> size ){

        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize, Sort.by(Sort.Direction.ASC, "batchid"));
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.ASC, "batchid"));
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
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        return "batch/browsebatch.html";
    }

    @GetMapping("/searchBatch")
    String searchBatch(@ModelAttribute( "batch" ) BatchVO batchVO, Model model){



        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", new BatchVO());
        model.addAttribute("batchs", new ArrayList<BatchVO>(1));
        return "batch/searchbatch.html";
    }

    @PostMapping("/searchBatch")
    String searchBatchPost(@ModelAttribute( "batch" ) BatchVO batchVO, Model model){



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

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("batch", batchVO);
        model.addAttribute("batchs", results);
        return "batch/searchbatch.html";
    }



    @PostMapping("/filtereditform")
    String viewFilterEditForm(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);
        return "batch/batch.html";
    }

    //    This will text the user a link to a menu pdf they can send people
    @PostMapping("/printmenu")
    String printmenu(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);
        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()

        boolean success = batchControllerHelper.sendTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchnumber, productTypeVO.priceadjustment)

        success ? model.addAttribute("successMessage", "Sent text message link successfully at: "+formattingUtil.getDateTimeForFileSystem()+" | With price adjustment:"+productTypeVO.priceadjustment) : model.addAttribute("errorMessage", "Error sending text message link at:" +formattingUtil.getDateTimeForFileSystem())

        return "batch/batch.html";
    }

    @PostMapping("/printmediamenu")
    String printmediamenu(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);
        // I need to do here is build a pdf / excel document, store it in uploads folder, then send a download link back to the user



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()

        boolean success = batchControllerHelper.sendMediaTextMessageWithDownloadLink(model, authentication.getPrincipal().username, batchnumber, productTypeVO.priceadjustment)

        success ? model.addAttribute("successMessage", "Sent text message link successfully at: "+formattingUtil.getDateTimeForFileSystem()+" | With price adjustment:"+productTypeVO.priceadjustment) : model.addAttribute("errorMessage", "Error sending text message link at:" +formattingUtil.getDateTimeForFileSystem())

        return "batch/batch.html";
    }


    @PostMapping("/likesearch")
    String viewLikeSearch(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, true);
        return "batch/batch.html";
    }

    @PostMapping("/generatebarcodes")
    String viewGenerateBarcodes(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        boolean success = false

        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.ALL:
                success = batchControllerHelper.generateAllBarcodesForBatch(batchnumber)
                break
            case controllerConstants.SINGLE_MENU:
                success = batchControllerHelper.generateSingleMenuBarcodesForBatch(batchnumber)
                break
        }



        if(success){
            model.addAttribute("successMessage","Successfully created barcodes for this batch. ")
        } else {
            model.addAttribute("errorMessage","Barcodes already exist for this batch. ")
        }


        return "batch/batch.html";
    }


    @PostMapping("/generateqrcodes")
    String viewGenerateQrcodes(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.ALL:
                batchControllerHelper.generateAllQrcodesForBatch(batchnumber)
                break
            case controllerConstants.SINGLE_MENU:
                batchControllerHelper.generateQrcodesForBatch(batchnumber)
                break
        }


        model.addAttribute("successMessage","Successfully created qr codes for this batch. ")

        return "batch/batch.html";
    }

    @PostMapping("/generateweightlabels")
    String generateWeightLabels(
            Model model,
            @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
            
            @RequestParam("editmode") String editmode,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size

    ){

        // todo: the "searchmenutype" field in dropdown is bound to productTypeVO which we are not currently using
        // todo: right now we are giving user option of "All" but we are not actually processing that option here.
        // todo: all we are doing is routing the button to this method and then generating the barcode sheets for this batch


        model = batchControllerHelper.processModel(model,  batchnumber, editmode, page, productTypeVO, true, false);

        switch(productTypeVO.menutype){
            case controllerConstants.SINGLE_PAGE:
                batchControllerHelper.generateSinglePageWeightLabelsForBatch(batchnumber)
                break
        }


        model.addAttribute("successMessage","Successfully created qr codes for this batch. ")

        return "batch/editbatch.html";
    }

    // todo: add the pagination crap here too
    @PostMapping ("/editBatch")
    String editBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                     @ModelAttribute( "searchproducttype" ) ProductTypeVO productTypeVO,
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
        techvvsAuthService.checkuserauth(model)
        batchControllerHelper.bindBatchTypes(model)
        return "batch/editbatch.html";
    }

    @PostMapping ("/createNewBatch")
    String createNewBatch(@ModelAttribute( "batch" ) BatchVO batchVO,
                                Model model,
                                HttpServletResponse response
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

        techvvsAuthService.checkuserauth(model)
        batchControllerHelper.bindBatchTypes(model)
        return "batch/batch.html";
    }




}
