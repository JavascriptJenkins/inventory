package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.constants.AppConstants;
import com.techvvs.inventory.jparepo.BatchTypeRepo;
import com.techvvs.inventory.jparepo.ProductRepo;
import com.techvvs.inventory.jparepo.ProductTypeRepo;
import com.techvvs.inventory.model.*;
import com.techvvs.inventory.modelnonpersist.FileVO;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.service.ExcelService;
import com.techvvs.inventory.service.auth.TechvvsAuthService;
import com.techvvs.inventory.service.controllers.TransactionService;
import com.techvvs.inventory.util.HeaderUtil;
import com.techvvs.inventory.util.TechvvsFileHelper;
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper;
import com.techvvs.inventory.viewcontroller.helper.FileViewHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    ProductTypeRepo productTypeRepo;

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

    @Autowired
    FileViewHelper fileViewHelper;

    @Autowired
    AppConstants appConstants;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    HeaderUtil headerUtil;
            
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
        return "/service/xlsxbatch.html";
    }

    // todo: we probably don't need the isqrmedia one here but whatever who cares fix it later
    @PostMapping("/media/upload")
    public String uploadMediaFile(Model model,
                                  @ModelAttribute("product") ProductVO productVO,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam("primary") Optional<String> isprimary,
                                  @RequestParam("video") Optional<String> isvideo,
                                  @RequestParam("document") Optional<String> isdocument,
                                  @RequestParam("photo") Optional<String> isphoto,
                                  @RequestParam("qrmedia") Optional<String> isqrmedia,
                                  RedirectAttributes attributes) {

        System.out.println("Starting file upload...");

        // Check if the file is empty
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Please select a file to upload.");
            System.out.println("No file selected for upload.");
            prepareEditForm(model, productVO);
            return "/product/editform.html";
        }

        // Sanitize the file name
        String originalFilename = file.getOriginalFilename();
        String sanitizedFileName = originalFilename != null
                ? originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
                : "uploaded_file";
        System.out.println("Sanitized file name: " + sanitizedFileName);

        boolean success = false;

        try {
            Path targetDirectory;
            if(isprimary.isPresent() && isprimary.get().equals("yes")){
                checkForJpgExtension(sanitizedFileName); // make sure it's a jpg or jpeg
                targetDirectory = Paths.get(
                        appConstants.UPLOAD_DIR_MEDIA,
                        appConstants.UPLOAD_DIR_PRODUCT,
                        String.valueOf(productVO.getProduct_id()),
                        appConstants.UPLOAD_DIR_PRODUCT_PRIMARY
                );
                sanitizedFileName = "primary.jpg";

            } else if(isvideo.isPresent() && isvideo.get().equals("yes")){
                checkForVideoExtension(sanitizedFileName);

                targetDirectory = Paths.get(
                        appConstants.UPLOAD_DIR_MEDIA,
                        appConstants.UPLOAD_DIR_PRODUCT,
                        String.valueOf(productVO.getProduct_id()),
                        appConstants.UPLOAD_DIR_PRODUCT_VIDEOS
                );
            } else if(isdocument.isPresent() && isdocument.get().equals("yes")){
                checkForPdfExtension(sanitizedFileName); //

                targetDirectory = Paths.get(
                        appConstants.UPLOAD_DIR_MEDIA,
                        appConstants.UPLOAD_DIR_PRODUCT,
                        String.valueOf(productVO.getProduct_id()),
                        appConstants.UPLOAD_DIR_PRODUCT_DOCUMENTS
                );
            } else if(isphoto.isPresent() && isphoto.get().equals("yes")){
                checkForJpgExtension(sanitizedFileName); // make sure it's a jpg or jpeg
                targetDirectory = Paths.get(
                        appConstants.UPLOAD_DIR_MEDIA,
                        appConstants.UPLOAD_DIR_PRODUCT,
                        String.valueOf(productVO.getProduct_id()),
                        appConstants.UPLOAD_DIR_PRODUCT_PHOTOS
                );
            }  else if(isqrmedia.isPresent() && isqrmedia.get().equals("yes")){
                targetDirectory = Paths.get(
                        appConstants.UPLOAD_DIR_MEDIA,
                        appConstants.UPLOAD_DIR_PRODUCT,
                        String.valueOf(productVO.getProduct_id())
                );
            } else {
                // default is upload the media to the qr directory
                targetDirectory = Paths.get(
                        appConstants.UPLOAD_DIR_MEDIA,
                        appConstants.UPLOAD_DIR_PRODUCT,
                        String.valueOf(productVO.getProduct_id())
                );
            }

            Files.createDirectories(targetDirectory);

            // Save the file
            Path targetFile = targetDirectory.resolve(sanitizedFileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File uploaded successfully to: " + targetFile.toString());
            success = true;
        } catch (IOException e) {
            System.err.println("File upload failed: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("errorMessage", "File upload failed. Please try again.");
            prepareEditForm(model, productVO);
            return "/product/editform.html";
        }

        if (success) {
            attributes.addFlashAttribute("successMessage", "File uploaded successfully: " + sanitizedFileName);
            attributes.addFlashAttribute("editmode", "no");
        } else {
            model.addAttribute("errorMessage", "Unexpected error occurred while uploading the file.");
        }

// Redirect to the product edit form
        String encodedProductNumber = URLEncoder.encode(String.valueOf(productVO.getProductnumber()), StandardCharsets.UTF_8);
        return "redirect:/product/editform?editmode=no&productnumber=" + encodedProductNumber;
    }

    // Utility method to prepare the edit form
    private void prepareEditForm(Model model, ProductVO productVO) {
        bindProductTypes(model);
        model.addAttribute("product", productVO);
        techvvsAuthService.checkuserauth(model);
    }


    void bindBatchTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<BatchTypeVO> batchTypeVOS = batchTypeRepo.findAll();
        model.addAttribute("batchtypes", batchTypeVOS);
    }

    String checkForJpgExtension(String sanitizedFileName) throws IOException {

        if(sanitizedFileName.toLowerCase().endsWith(".jpg")){
            return sanitizedFileName;
        }

        // Check the file extension
        if (sanitizedFileName.toLowerCase().endsWith(".jpeg")) {
            // Replace .jpeg with .jpg
            return sanitizedFileName.substring(0, sanitizedFileName.length() - 5) + ".jpg";
        } else if (!sanitizedFileName.toLowerCase().endsWith(".jpg")) {
            // Throw exception if it does not end with .jpg
            throw new IOException("Sanitized file name must end with .jpg: " + sanitizedFileName);
        }
        return sanitizedFileName;

    }

    String checkForVideoExtension(String sanitizedFileName) throws IOException {

        if (sanitizedFileName.toLowerCase().endsWith(".mp4") || sanitizedFileName.toLowerCase().endsWith(".mov")) {
            return sanitizedFileName;
        }

        // If the extension is not valid, throw an exception
        throw new IOException("Sanitized file name must end with .mp4 or .mov: " + sanitizedFileName);
    }

    String checkForPdfExtension(String sanitizedFileName) throws IOException {

        if (sanitizedFileName.toLowerCase().endsWith(".pdf")) {
            return sanitizedFileName;
        }

        // If the extension is not valid, throw an exception
        throw new IOException("Sanitized file name must end with .pdf: " + sanitizedFileName);
    }



    void bindProductTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypes", productTypeVOS);
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
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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



    // This file download is for links that are in the application being used by a logged in user
    @RequestMapping(value="/smsdownload", method=RequestMethod.GET)
    public void smsdownload(@RequestParam("filename") String filename,
                             HttpServletResponse response,
                            Model model
    ) {
        File file;

        try {

            // todo: set .xlsx filetype here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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
        techvvsAuthService.checkuserauth(model);


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
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
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

    @RequestMapping(value="/smsdownload3", method=RequestMethod.GET)
    public void smsdownload3(
            @RequestParam("filename") String filename,
            @RequestParam("product_id") String product_id,
            @RequestParam("customJwtParameter") Optional<String> customJwtParameter,
            HttpServletResponse response
    ) {

        // todo: make this jwt check actually do something
        // check the token here, will throw 403 if the token is expired
        //jwtTokenProvider.validateTokenForSmsPhoneDownload(customJwtParameter.orElse(""));

        String fullpath = Paths.get(appConstants.UPLOAD_DIR_MEDIA, appConstants.UPLOAD_DIR_PRODUCT, product_id, filename).toString();

        System.out.println("Constructed file path: " + fullpath);

        File file;

        try {

            // this attaches correct filetype header so the browser knows what kind of file is coming back
            headerUtil.setFileReturnHeader(filename, response);

            file = new File(fullpath); // pass in full filepath here

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


    @RequestMapping(value = "/qrzipmediadownload", method = RequestMethod.GET)
    public void qrzipmediadownload(
            @RequestParam("productid") String productid,
            HttpServletResponse response
    ) {
        ConcurrentHashMap<String, Long> rateLimiter = new ConcurrentHashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        long currentTime = System.currentTimeMillis();
        long lastAccessTime = rateLimiter.getOrDefault(username, 0L);

        if ((currentTime - lastAccessTime) < TimeUnit.SECONDS.toMillis(5)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You can only request this endpoint once every 5 seconds.");
        }
        rateLimiter.put(username, currentTime);

        Path directoryPath = Paths.get(appConstants.UPLOAD_DIR_MEDIA, appConstants.UPLOAD_DIR_PRODUCT, productid);
        if (!Files.exists(directoryPath)) {
            throw new RuntimeException("Directory does not exist: " + directoryPath);
        }

        System.out.println("Zipping folder: " + directoryPath);

        ProductVO productVO = productRepo.getById(Integer.valueOf(productid));

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + productVO.getName()+"_media_"+productid + ".zip");
        response.setStatus(HttpServletResponse.SC_OK); // Explicitly set a 200 status


        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream(), 64 * 1024))) {
            Files.walk(directoryPath, 10).filter(Files::isRegularFile).forEach(file -> {
                try (InputStream fileInputStream = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ), 64 * 1024)) {
                    System.out.println("Adding file to ZIP: " + file);
                    ZipEntry zipEntry = new ZipEntry(directoryPath.relativize(file).toString());
                    zipOut.putNextEntry(zipEntry);

                    byte[] buffer = new byte[64 * 1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                    zipOut.closeEntry();
                } catch (IOException e) {
                    System.err.println("Error processing file: " + file + ". Skipping file.");
                }
            });
            zipOut.flush();
            zipOut.finish();
        } catch (IOException ex) {
            System.err.println("Error creating zip file: " + ex.getMessage());
            throw new RuntimeException("IOError creating zip file", ex);
        }
    }




    // this should be used for public facing whitelisted downloads like coa's for example
    @RequestMapping(value="/publicdownload", method=RequestMethod.GET)
    public void publicdownload(@RequestParam("filename") String filename,
                            HttpServletResponse response
    ) {
        File file;

        String basefilename = filename;

        try {

            filename = fileViewHelper.buildFileNameForPublicDownload(appConstants.COA_DIR, filename);

            // todo: set filetype based on file extension here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }

            response.setHeader("Content-Disposition","attachment; filename="+basefilename);

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


    // this should be used for static files that need to be served to logged in users of the app
    // this means they are logged with a valid cookie
    @RequestMapping(value="/inappdownload", method=RequestMethod.GET)
    public void inappdownload(@RequestParam("filename") String filename,
                               HttpServletResponse response
    ) {
        File file;

        String basefilename = filename;

        try {

            filename = fileViewHelper.buildFileNameForPublicDownload(appConstants.FILES_FOR_GLOBAL_USER_DOWNLOAD_DIR, filename);

            // todo: set filetype based on file extension here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }

            response.setHeader("Content-Disposition","attachment; filename="+basefilename);

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



    @RequestMapping(value="/privatedeliverydownload", method=RequestMethod.GET)
    public void privatedeliverydownload(
            @RequestParam("filename") String filename,
                               HttpServletResponse response
    ) {


        File file;
        filename = filename.replaceAll("'", "");
        String deliveryid = extractDeliveryNumber(filename);

        String basefilename = filename;

        try {

            filename = fileViewHelper.buildFileNameForPrivateDeliveryDownload(
                    appConstants.DELIVERY_DIR+deliveryid+appConstants.BARCODES_ALL_DIR,
                    filename);

            // todo: set filetype based on file extension here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }

            response.setHeader("Content-Disposition","attachment; filename="+basefilename);

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

    @RequestMapping(value="/privateqrmediadownload", method=RequestMethod.GET)
    public void privateqrmediadownload(
            @RequestParam("productid") String productid,
            @RequestParam("name") String name,
            @RequestParam("number") String number,
            @RequestParam("batchnumber") String batchnumber,
            @RequestParam("batchname") String batchname,
            HttpServletResponse response
    ) {


        File file;
        productid = productid.replaceAll("'", "");
        name = name.replaceAll("'", "");
        number = number.replaceAll("'", "");
        batchnumber = batchnumber.replaceAll("'", "");
        batchname = batchname.replaceAll("'", "");
       // String deliveryid = extractDeliveryNumber(filename);

        //                         // // Path path = Paths.get(appConstants.UPLOAD_DIR_MEDIA+appConstants.UPLOAD_DIR_PRODUCT+"/"+productVO.getProduct_id()+"/"+fileName);
        //            document.save(appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+appConstants.QR_MEDIA_DIR+appConstants.filenameprefix_qr_media+filename+".pdf");
        String basefilename = appConstants.filenameprefix_qr_media+batchname+"-"+batchnumber+".pdf";
        String filename = "";
        try {

            filename = fileViewHelper.buildFileNameForPrivateQrMediaDownload(
                    batchnumber+appConstants.QR_MEDIA_DIR,
                    basefilename);

            // todo: set filetype based on file extension here
            if(filename.contains(".pdf")){
                response.setContentType("application/pdf");
            } else if(filename.contains(".xlsx")){
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }

            response.setHeader("Content-Disposition","attachment; filename="+basefilename);

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


    public static String extractDeliveryNumber(String filename) {
        // Define the regex pattern
        String pattern = "(?<=-)\\d+(?=\\.pdf)";

        // Create a Pattern object
        Pattern regex = Pattern.compile(pattern);

        // Create a Matcher object
        Matcher matcher = regex.matcher(filename);

        // Find and return the match if available
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null; // Or handle the case where no match is found
        }
    }

}
