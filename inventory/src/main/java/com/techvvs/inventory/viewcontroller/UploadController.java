package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.jparepo.BatchTypeRepo;
import com.techvvs.inventory.model.BatchTypeVO;
import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.modelnonpersist.FileVO;
import com.techvvs.inventory.util.TechvvsFileHelper;
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

    @GetMapping("/")
    public String homepage() {
        return "index";
    }

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    BatchTypeRepo batchTypeRepo;

    @PostMapping("/upload")
    public String uploadFile(@ModelAttribute( "batch" ) BatchVO batchVO,
                             Model model,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes attributes,
                             @RequestParam("customJwtParameter") String customJwtParameter) {

        System.out.println("file upload 1");
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage","Please select a file to upload.");
            System.out.println("file upload 2");
            model.addAttribute("batch", batchVO);
            bindBatchTypes(model);
            model.addAttribute("customJwtParameter",customJwtParameter);
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
            model.addAttribute("customJwtParameter",customJwtParameter);
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
        model.addAttribute("customJwtParameter",customJwtParameter);
        return "service/batch.html";
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
                             RedirectAttributes attributes,
                             @RequestParam("customJwtParameter") String customJwtParameter) {

        System.out.println("file upload 1");
        // check if file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage","Please select a file to upload.");
            model.addAttribute("batch", batchVO);
            bindBatchTypes(model);
            model.addAttribute("customJwtParameter",customJwtParameter);
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
            model.addAttribute("customJwtParameter",customJwtParameter);
            return "editforms.html";
        }


        // write code here to see how many files have been uploaded related to the batchnumber on batch record

        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(batchVO.getBatchnumber(), UPLOAD_DIR);



        // return success response
        model.addAttribute("successMessage","You successfully uploaded " + fileName + '!');
        model.addAttribute("batch", batchVO);
        bindBatchTypes(model);
        model.addAttribute("filelist", filelist);
        model.addAttribute("customJwtParameter",customJwtParameter);
        return "editforms.html";
    }

    // TODO: make getting a download link popup so people click a link in a popup box
    // note: must return null otherwise file download sucks
    @RequestMapping(value="/download", method=RequestMethod.GET)
    public void downloadFile(@RequestParam("filename") String filename,
                                       HttpServletResponse response,
                                       @RequestParam("customJwtParameter") String customJwtParameter,
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


            model.addAttribute("customJwtParameter",customJwtParameter);
            System.out.println("has value: "+batchVO);

            model.addAttribute("customJwtParameter",customJwtParameter);
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
            model.addAttribute("customJwtParameter",customJwtParameter);
            throw new RuntimeException("IOError writing file to output stream");
        }


//        return "redirect: /newform/viewNewForm";
    }

}
