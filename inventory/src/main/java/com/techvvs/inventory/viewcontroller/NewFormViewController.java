//package com.techvvs.inventory.viewcontroller;
//
//import com.google.common.base.CharMatcher;
//import com.techvvs.inventory.jparepo.ProcessDataRepo;
//import com.techvvs.inventory.model.ProcessDataDAO;
//import com.techvvs.inventory.modelnonpersist.FileVO;
//import com.techvvs.inventory.util.TechvvsFileHelper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@RequestMapping("/batch")
//@Controller
//public class NewFormViewController {
//
//    private final String UPLOAD_DIR = "./uploads/";
//
//    @Autowired
//    HttpServletRequest httpServletRequest;
//
//    @Autowired
//    TechvvsFileHelper techvvsFileHelper;
//
//    @Autowired
//    ProcessDataRepo processDataRepo;
//
//    SecureRandom secureRandom = new SecureRandom();
//
//
//    //default home mapping
//    @GetMapping
//    String viewNewForm(@ModelAttribute( "processdata" ) ProcessDataDAO processDataDAO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){
//
//        System.out.println("customJwtParam on batch controller: "+customJwtParameter);
//
//        ProcessDataDAO processDataDAOToBind;
//        if(processDataDAO != null && processDataDAO.getId() != null){
//            processDataDAOToBind = processDataDAO;
//        } else {
//            processDataDAOToBind = new ProcessDataDAO();
//            processDataDAOToBind.setserveattempts(0);
//            processDataDAOToBind.setFilenumber(secureRandom.nextInt(10000000));
//        }
//
//        model.addAttribute("disableupload","true"); // disable uploading a file until we actually have a record submitted successfully
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("processdata", processDataDAOToBind);
//        return "batch.html";
//    }
//
//    @GetMapping("/browseProcessData")
//    String browseProcessData(@ModelAttribute( "processdata" ) ProcessDataDAO processDataDAO,
//                             Model model,
//                             @RequestParam("customJwtParameter") String customJwtParameter,
//                             @RequestParam("page") Optional<Integer> page,
//                             @RequestParam("size") Optional<Integer> size ){
//
//        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
//        //pagination
//        int currentPage = page.orElse(0);
//        int pageSize = 5;
//        Pageable pageable;
//        if(currentPage == 0){
//            pageable = PageRequest.of(0 , pageSize);
//        } else {
//            pageable = PageRequest.of(currentPage - 1, pageSize);
//        }
//
//        Page<ProcessDataDAO> pageOfProcessData = processDataRepo.findAll(pageable);
//
//        int totalPages = pageOfProcessData.getTotalPages();
//
//        List<Integer> pageNumbers = new ArrayList<>();
//
//        while(totalPages > 0){
//            pageNumbers.add(totalPages);
//            totalPages = totalPages - 1;
//        }
//
//        model.addAttribute("pageNumbers", pageNumbers);
//        model.addAttribute("page", currentPage);
//        model.addAttribute("size", pageOfProcessData.getTotalPages());
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("processdata", new ProcessDataDAO());
//        model.addAttribute("processdataPage", pageOfProcessData);
//        return "browseforms.html";
//    }
//
//    @GetMapping("/searchProcessData")
//    String searchProcessData(@ModelAttribute( "processdata" ) ProcessDataDAO processDataDAO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){
//
//        System.out.println("customJwtParam on batch controller: "+customJwtParameter);
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("processdata", new ProcessDataDAO());
//        model.addAttribute("processdatas", new ArrayList<ProcessDataDAO>(1));
//        return "searchforms.html";
//    }
//
//    @PostMapping("/searchProcessData")
//    String searchProcessDataPost(@ModelAttribute( "processdata" ) ProcessDataDAO processDataDAO, Model model, @RequestParam("customJwtParameter") String customJwtParameter){
//
//        System.out.println("customJwtParam on batch controller: "+customJwtParameter);
//
//        List<ProcessDataDAO> results = new ArrayList<ProcessDataDAO>();
//        if(processDataDAO.getFilenumber() != null){
//            System.out.println("Searching data by getFilenumber");
//            results = processDataRepo.findAllByFilenumber(processDataDAO.getFilenumber());
//        }
//        if(processDataDAO.getFname() != null && results.size() == 0){
//            System.out.println("Searching data by getFname");
//            results = processDataRepo.findAllByFname(processDataDAO.getFname());
//        }
//        if(processDataDAO.getLname() != null && results.size() == 0){
//            System.out.println("Searching data by getLname");
//            results = processDataRepo.findAllByLname(processDataDAO.getLname());
//        }
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("processdata", processDataDAO);
//        model.addAttribute("processdatas", results);
//        return "searchforms.html";
//    }
//
//    @GetMapping("/editform")
//    String viewEditForm(
//                    Model model,
//                    @RequestParam("customJwtParameter") String customJwtParameter,
//                    @RequestParam("filenumber") String filenumber){
//
//        System.out.println("customJwtParam on batch controller: "+customJwtParameter);
//
//        List<ProcessDataDAO> results = new ArrayList<ProcessDataDAO>();
//        if(filenumber != null){
//            System.out.println("Searching data by filenumber");
//            results = processDataRepo.findAllByFilenumber(Integer.valueOf(filenumber));
//        }
//
//        // check to see if there are files uploaded related to this filenumber
//        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(filenumber), UPLOAD_DIR);
//
//        if(filelist.size() > 0){
//            model.addAttribute("filelist", filelist);
//        } else {
//            model.addAttribute("filelist", null);
//        }
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        model.addAttribute("processdata", results.get(0));
//        return "editforms.html";
//    }
//
//    @PostMapping ("/editProcessData")
//    String editProcessData(@ModelAttribute( "processdata" ) ProcessDataDAO processDataDAO,
//                                Model model,
//                                HttpServletResponse response,
//                                @RequestParam("customJwtParameter") String customJwtParameter
//    ){
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("----------------------- START AUTH INFO ");
//        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
//        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
//        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
//        System.out.println("----------------------- END AUTH INFO ");
//
//        String errorResult = validateNewFormInfo(processDataDAO);
//
//        // Validation
//        if(!errorResult.equals("success")){
//            model.addAttribute("errorMessage",errorResult);
//        } else {
//
//            // when creating a new processData entry, set the last attempt visit to now - this may change in future
//            processDataDAO.setUpdatedtimestamp(LocalDateTime.now());
//
//            if(processDataDAO.getActioncounter1() != null){
//                processDataDAO.setActioncounter1(processDataDAO.getActioncounter1()+1);
//            } else {
//                processDataDAO.setActioncounter1(1);
//            }
//
//            ProcessDataDAO result = processDataRepo.save(processDataDAO);
//
//            // check to see if there are files uploaded related to this filenumber
//            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(processDataDAO.getFilenumber(), UPLOAD_DIR);
//            if(filelist.size() > 0){
//                model.addAttribute("filelist", filelist);
//            } else {
//                model.addAttribute("filelist", null);
//            }
//
//            model.addAttribute("successMessage","Record Successfully Saved.");
//            model.addAttribute("processdata", result);
//        }
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        return "editforms.html";
//    }
//
//    @PostMapping ("/createNewProcessData")
//    String createNewProcessData(@ModelAttribute( "processdata" ) ProcessDataDAO processDataDAO,
//                                Model model,
//                                HttpServletResponse response,
//                                @RequestParam("customJwtParameter") String customJwtParameter
//    ){
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("----------------------- START AUTH INFO ");
//        System.out.println("authentication.getCredentials: "+authentication.getCredentials());
//        System.out.println("authentication.getPrincipal: "+authentication.getPrincipal());
//        System.out.println("authentication.getAuthorities: "+authentication.getAuthorities());
//        System.out.println("----------------------- END AUTH INFO ");
//
//        String errorResult = validateNewFormInfo(processDataDAO);
//
//        // Validation
//        if(!errorResult.equals("success")){
//            model.addAttribute("disableupload","true"); // if there is an error submitting the new form we keep this disabled
//            model.addAttribute("errorMessage",errorResult);
//        } else {
//
//            // when creating a new processData entry, set the last attempt visit to now - this may change in future
//            processDataDAO.setLastattemptvisit(LocalDateTime.now());
//            processDataDAO.setCreatetimestamp(LocalDateTime.now());
//            processDataDAO.setUpdatedtimestamp(LocalDateTime.now());
//
//            if(processDataDAO.getActioncounter1() != null){
//                processDataDAO.setActioncounter1(processDataDAO.getActioncounter1()+1);
//            } else {
//                processDataDAO.setActioncounter1(1);
//            }
//
//            ProcessDataDAO result = processDataRepo.save(processDataDAO);
//
//            model.addAttribute("successMessage","Record Successfully Saved. ");
//            model.addAttribute("processdata", result);
//        }
//
//        model.addAttribute("customJwtParameter", customJwtParameter);
//        return "batch.html";
//    }
//
//    String validateNewFormInfo(ProcessDataDAO processDataDAO){
//
//        if(processDataDAO.getFname() != null &&
//                (processDataDAO.getFname().length() > 250
//                || processDataDAO.getFname().length() < 1)
//        ){
//            return "first name must be between 1-250 characters. ";
//        }
//
//        if(processDataDAO.getLname() != null &&
//                (processDataDAO.getLname().length() > 250
//                || processDataDAO.getLname().length() < 1)
//        ){
//            return "last name must be between 1-250 characters. ";
//        }
//
//        // get all the integers out of the phone string (guava is faster than regex)
//        if(processDataDAO.getPhone() != null){
//            String theDigits = CharMatcher.inRange('0', '9').retainFrom(processDataDAO.getPhone());
//            processDataDAO.setPhone(theDigits);
//        }
//
//        if(processDataDAO.getPhone() != null &&
//                (processDataDAO.getPhone().length() > 11
//                || processDataDAO.getPhone().length() < 10
//                || processDataDAO.getPhone().contains("-")
//                || processDataDAO.getPhone().contains("."))
//        ){
//            return "enter 10 or 11 digit phone number (special characters are ignored).  ex. 18884445555";
//        } else if (processDataDAO.getPhone() != null) {
//            processDataDAO.setPhone(processDataDAO.getPhone().trim());
//            processDataDAO.setPhone(processDataDAO.getPhone().replaceAll(" ",""));
//            processDataDAO.setPhone(processDataDAO.getPhone().replaceAll("\\)",""));
//        }
//
//        if(processDataDAO.getAge() != null && (processDataDAO.getAge() > 140
//                || processDataDAO.getPhone().length() < 1)
//        ){
//            return "age must be between 1-140. ";
//        }
//
//
//        if(processDataDAO.getNotes() != null && (processDataDAO.getNotes().length() > 1000)
//        ){
//            return "Notes must be less than 1000 characters";
//        }
//
//        if(processDataDAO.getAddress1() != null && (processDataDAO.getAddress1().length() > 200)
//        ){
//            return "address1 must be less than 200 characters";
//        }
//
//        if(processDataDAO.getAddress2() != null && processDataDAO.getAddress2().length() > 200
//        ){
//            return "address2 must be less than 200 characters";
//        }
//
//        if(processDataDAO.getRace() != null && processDataDAO.getRace().length() > 20
//        ){
//            return "race must be less than 20 characters";
//        }
//
//        if(processDataDAO.getCity() != null && processDataDAO.getCity().length() > 100
//        ){
//            return "city must be less than 100 characters";
//        }
//
//        if(processDataDAO.getState() != null && processDataDAO.getState().length() > 100
//        ){
//            return "state must be less than 100 characters";
//        }
//
//        if(processDataDAO.getZipcode() != null && processDataDAO.getZipcode().length() > 10
//        ){
//            return "zipcode must be less than 100 characters";
//        }
//
//        if(processDataDAO.getHeightfeet() != null && processDataDAO.getHeightfeet() > 10
//        ){
//            return "height feet must be less than 10";
//        }
//
//        if(processDataDAO.getHeightinches() != null && processDataDAO.getHeightinches() > 12
//        ){
//            return "height inches must be less than 12";
//        }
//
//        if(processDataDAO.getHaircolor() != null && processDataDAO.getHaircolor().length() > 20
//        ){
//            return "hair color must be less than 20 characters";
//        }
//
//        return "success";
//    }
//
//
//}
