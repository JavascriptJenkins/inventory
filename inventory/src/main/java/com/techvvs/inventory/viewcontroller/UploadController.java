package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.constants.AppConstants;
import com.techvvs.inventory.jparepo.BatchTypeRepo;
import com.techvvs.inventory.model.BatchTypeVO;
import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.TransactionVO;
import com.techvvs.inventory.modelnonpersist.FileVO;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.service.ExcelService;
import com.techvvs.inventory.service.auth.TechvvsAuthService;
import com.techvvs.inventory.service.controllers.TransactionService;
import com.techvvs.inventory.util.TechvvsFileHelper;
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RequestMapping("/file")
@Controller
public class UploadController {

    private final String UPLOAD_DIR = "./uploads/";
    private final String UPLOAD_DIR_XLSX = "./uploads/xlsx/";

    @GetMapping("/")
    public String homepage() {
        return "index";
    }

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchTypeRepo batchTypeRepo;

    @Autowired
    ExcelService excelService;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    TransactionService transactionService;

    @Autowired
    TechvvsAuthService techvvsAuthService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;
            
    @PostMapping("/upload")
    public String uploadFile(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes attributes) {

        System.out.println("file upload 1");
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage","Please select a file to upload.");
            System.out.println("file upload 2");
            model.addAttribute("batch", batchVO);
            bindBatchTypes(model);
            techvvsAuthService.checkuserauth(model);
            return "/service/batch.html";
        }
        System.out.println("file upload 3");
        String fileName = "";
        if(file.getOriginalFilename() != null){
            System.out.println("file upload 4");
//            file.getOriginalFilename() =  file.getOriginalFilename().replaceAll("-","");
            // normalize the file path
            fileName = "/"+batchVO.getBatchnumber()+"---"+StringUtils.cleanPath(file.getOriginalFilename());
        }

        // save the file on the local file system
        try {
            System.out.println("file upload 5");
            Path path = Paths.get(UPLOAD_DIR + fileName);
            System.out.println("file upload 6");
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("file upload 7");
        } catch (IOException e) {
            System.out.println("file upload 8");
            e.printStackTrace();
            model.addAttribute("errorMessage","file upload failed");
            bindBatchTypes(model);
            techvvsAuthService.checkuserauth(model);
            return "service/batch.html";
        }


        // write code here to see how many files have been uploaded related to the batchnumber on batch record

        System.out.println("file upload 9");
        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), UPLOAD_DIR);

        System.out.println("file upload 10");


        // return success response
        model.addAttribute("successMessage","You successfully uploaded " + fileName + '!');
        model.addAttribute("batch", batchVO);
        bindBatchTypes(model);
        model.addAttribute("filelist", filelist);
        techvvsAuthService.checkuserauth(model);
        return "service/batch.html";
    }


    // This method is for uploading XLSX file price sheets
    @PostMapping("/xlsx/upload")
    public String uploadXlsxFile(Model model,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes attributes) {

        System.out.println("file upload 1");
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage","Please select a file to upload.");
            System.out.println("file upload 2");
            techvvsAuthService.checkuserauth(model);
            return "/service/xlsxbatch.html";
        }
        System.out.println("file upload 3");
        String fileName = "";
        if(file.getOriginalFilename() != null){
            System.out.println("file upload 4");
            // sanitize the file name
            fileName = techvvsFileHelper.sanitizeMultiPartFileName(file);
        }

        // save the file on the local file system
        try {
            System.out.println("file upload 5");
            Path path = Paths.get(UPLOAD_DIR_XLSX + fileName);
            System.out.println("file upload 6");
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("file upload 7");
        } catch (IOException e) {
            System.out.println("file upload 8");
            e.printStackTrace();
            model.addAttribute("errorMessage","file upload failed");
            techvvsAuthService.checkuserauth(model);
            return "service/xlsxbatch.html";
        }


        System.out.println("file upload 10");


        // now read the xlsx file and create a batch based on it in the database
        boolean success = excelService.importExcelPriceSheet(fileName); // passing in filename like "filename.xlsx"

        if(success){
            model.addAttribute("successMessage","Batch created, qr codes created, barcodes created, and default menu created from file: " + fileName + '!');

        } else {
            // this should never happen
            model.addAttribute("errorMessage","Problem creating batch in database from file: " + fileName + '!');
        }

        techvvsAuthService.checkuserauth(model);
        return "/batch/browsebatch.html";
    }

    void bindBatchTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<BatchTypeVO> batchTypeVOS = batchTypeRepo.findAll();
        model.addAttribute("batchtypes", batchTypeVOS);
    }

    @PostMapping("/upload2")
    public String uploadFile2(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes attributes) {

        System.out.println("file upload 1");
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage","Please select a file to upload.");
            model.addAttribute("batch", batchVO);
            bindBatchTypes(model);
            techvvsAuthService.checkuserauth(model);
            return "/editforms.html";
        }
        String fileName = "";
        if(file.getOriginalFilename() != null){
//            file.getOriginalFilename() =  file.getOriginalFilename().replaceAll("-","");
            // normalize the file path
            fileName = "/"+batchVO.getBatchnumber()+"---"+StringUtils.cleanPath(file.getOriginalFilename());
        }

        // save the file on the local file system
        try {
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage","file upload failed");
            model.addAttribute("batch", batchVO);
            bindBatchTypes(model);
            techvvsAuthService.checkuserauth(model);
            return "editforms.html";
        }


        // write code here to see how many files have been uploaded related to the batchnumber on batch record

        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), UPLOAD_DIR);



        // return success response
        model.addAttribute("successMessage","You successfully uploaded " + fileName + '!');
        model.addAttribute("batch", batchVO);
        bindBatchTypes(model);
        model.addAttribute("filelist", filelist);
        techvvsAuthService.checkuserauth(model);
        return "editforms.html";
    }

    // CHATGPT: make this method open and execute in a new tab or a popup window, whatever is easier
    // note: must return null otherwise file download sucks
    @RequestMapping(value="/download", method=RequestMethod.GET)
    public void downloadFile(@RequestParam("filename") String filename,
                                       HttpServletResponse response,
                                       
                                       Model model,
                                        @ModelAttribute( "batch" ) BatchVO batchVO
                               ) {
        File file;

        try {
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            }

            response.setHeader("Content-Disposition","attachment; filename="+filename);

            file = new File(UPLOAD_DIR+filename);

            byte[] fileContent = Files.readAllBytes(file.toPath());


            // get your file as InputStream
            InputStream is = new ByteArrayInputStream(fileContent);


            techvvsAuthService.checkuserauth(model);
            System.out.println("has value: "+batchVO);

            techvvsAuthService.checkuserauth(model);
            model.addAttribute("batch",batchVO);
            model.addAttribute("disableupload","true");
            List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), UPLOAD_DIR);
            model.addAttribute("filelist",filelist);


            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
        } catch (IOException ex) {
            System.out.println("Error writing file to output stream. Filename was: " +filename);
            System.out.println("Error writing file to output stream. exception: " +ex.getMessage());
            techvvsAuthService.checkuserauth(model);
            throw new RuntimeException("IOError writing file to output stream");
        }


//        return "redirect: /newform/viewNewForm";
    }


    // CHATGPT: make this method open and execute in a new tab or a popup window, whatever is easier
    // note: must return null otherwise file download sucks
    @RequestMapping(value="/simple/download", method=RequestMethod.GET)
    public void downloadSimpleFile(@RequestParam("filename") String filename,
                             HttpServletResponse response,
                             
                             @RequestParam("directory") String directory,
                             @RequestParam("batchid") String batchid,
                             Model model
    ) {


        BatchVO batchVO =batchControllerHelper.loadBatch(batchid, model);

        File file;

        try {
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            }

            response.setHeader("Content-Disposition","attachment; filename="+filename);

            // todo: enforce that the incoming directory matches the allowed directories....
            file = new File(directory+"/"+filename);

            byte[] fileContent = Files.readAllBytes(file.toPath());


            // get your file as InputStream
            InputStream is = new ByteArrayInputStream(fileContent);


            techvvsAuthService.checkuserauth(model);

            techvvsAuthService.checkuserauth(model);
            model.addAttribute("disableupload","true");


            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
        } catch (IOException ex) {
            System.out.println("Error writing file to output stream. Filename was: " +filename);
            System.out.println("Error writing file to output stream. exception: " +ex.getMessage());
            techvvsAuthService.checkuserauth(model);
            throw new RuntimeException("IOError writing file to output stream");
        }


//        return "redirect: /newform/viewNewForm";
    }


    // CHATGPT: make this method open and execute in a new tab or a popup window, whatever is easier
    // note: must return null otherwise file download sucks
    @RequestMapping(value="/invoice/download", method=RequestMethod.GET)
    public void downloadInvoiceFile(@RequestParam("filename") String filename,
                                   HttpServletResponse response,
                                   
                                   @RequestParam("directory") String directory,
                                   @RequestParam("transactionid") String transactionid,
                                   Model model
    ) {


        TransactionVO transactionVO = transactionService.getExistingTransaction(Integer.valueOf(transactionid));
        model.addAttribute("transaction", transactionVO);

        File file;

        try {
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            }

            response.setHeader("Content-Disposition","attachment; filename="+filename);

            // todo: enforce that the incoming directory matches the allowed directories....
            file = new File(directory+"/"+filename);

            byte[] fileContent = Files.readAllBytes(file.toPath());


            // get your file as InputStream
            InputStream is = new ByteArrayInputStream(fileContent);


            // todo: maybe adding this paramter is the problem
            techvvsAuthService.checkuserauth(model);

            techvvsAuthService.checkuserauth(model);
            model.addAttribute("disableupload","true");


            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
        } catch (IOException ex) {
            System.out.println("Error writing file to output stream. Filename was: " +filename);
            System.out.println("Error writing file to output stream. exception: " +ex.getMessage());
            techvvsAuthService.checkuserauth(model);
            throw new RuntimeException("IOError writing file to output stream");
        }


//        return "redirect: /newform/viewNewForm";
    }


    // TODO: make getting a download link popup so people click a link in a popup box
    // note: must return null otherwise file download sucks
    @RequestMapping(value="/smsdownload", method=RequestMethod.GET)
    public void smsdownload(@RequestParam("filename") String filename,
                             HttpServletResponse response
    ) {
        File file;

        try {

            // todo: set .xlsx filetype here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            }

            response.setHeader("Content-Disposition","attachment; filename="+filename);

            file = new File(filename);

            byte[] fileContent = Files.readAllBytes(file.toPath());

            // get your file as InputStream
            InputStream is = new ByteArrayInputStream(fileContent);

            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();

        } catch (IOException ex) {
            System.out.println("Error writing file to output stream. Filename was: " +filename);
            System.out.println("Error writing file to output stream. exception: " +ex.getMessage());
            throw new RuntimeException("IOError writing file to output stream");
        }


//        return "redirect: /newform/viewNewForm";
    }

    // this one checks the token to secure the download
    @RequestMapping(value="/smsdownload2", method=RequestMethod.GET)
    public void smsdownload2(
            @RequestParam("filename") String filename,
            @RequestParam("customJwtParameter") String customJwtParameter,
                            HttpServletResponse response
    ) {

        // check the token here, will throw 403 if the token is expired
        jwtTokenProvider.validateTokenForSmsPhoneDownload(customJwtParameter);

        File file;

        try {

            // todo: set .xlsx filetype here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            }

            response.setHeader("Content-Disposition","attachment; filename="+filename);

            file = new File(filename);

            byte[] fileContent = Files.readAllBytes(file.toPath());

            // get your file as InputStream
            InputStream is = new ByteArrayInputStream(fileContent);

            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();

        } catch (IOException ex) {
            System.out.println("Error writing file to output stream. Filename was: " +filename);
            System.out.println("Error writing file to output stream. exception: " +ex.getMessage());
            throw new RuntimeException("IOError writing file to output stream");
        }


//        return "redirect: /newform/viewNewForm";
    }


}
