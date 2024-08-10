package com.techvvs.inventory.util;

import com.techvvs.inventory.constants.AppConstants;
import com.techvvs.inventory.model.PaymentVO;
import com.techvvs.inventory.model.TransactionVO;
import com.techvvs.inventory.modelnonpersist.FileVO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class TechvvsFileHelper {

    @Autowired
    AppConstants appConstants;

    @Autowired
    FormattingUtil formattingUtil;

    public List<FileVO> getFilesByFileNumber(Integer filenumber, String uploaddir){
        List<FileVO> filelist = new ArrayList<>(2);

        try{
            Path path = Paths.get(uploaddir);
            File dir = new File(String.valueOf(path));
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    // Do something with child
                    child.getAbsoluteFile();
                    child.getAbsoluteFile().getName();
                    if(child.getAbsoluteFile().getName().contains(String.valueOf(filenumber))){
                        FileVO fileVO = new FileVO();
                        fileVO.setFilename(child.getAbsoluteFile().getName());
                        fileVO.setDirectory(child.getParent()); // .\topdir\2011253\barcodes\all
                        filelist.add(fileVO); // add it to a nonpersisted list that will be displayed on the ui
                    }
                }
            } else {
                System.out.println("Error getting list of files, should never happen. ");
                // Handle the case where dir is not really a directory.
                // Checking dir.isDirectory() above would not be sufficient
                // to avoid race conditions with another process that deletes
                // directories.
            }
        } catch(Exception ex){
            System.out.println("listing files Exception");
            System.out.println("Caught exception listing files: "+ex.getMessage());
        }

        return filelist;
    }


    // get a list back with all the sub directories inside a top level directory
    public List<FileVO> getFilesBySubDirectory(String topLevelDir) {
        List<FileVO> fileList = new ArrayList<>();

        try {
            Path path = Paths.get(topLevelDir);
            File dir = new File(String.valueOf(path));
            File[] subDirectories = dir.listFiles(File::isDirectory);
            if (subDirectories != null) {
                for (File subDir : subDirectories) {
                    File[] files = subDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            FileVO fileVO = new FileVO();
                            fileVO.setFilename(file.getName());
                            fileVO.setDirectory(file.getParent()); // .\topdir\2011253\barcodes\all
                            fileList.add(fileVO);
                        }
                    }
                }
            } else {
                System.out.println("Error getting list of subdirectories, should never happen. ");
            }
        } catch (Exception ex) {
            System.out.println("Exception in getFilesBySubDirectory");
            System.out.println("Caught exception: " + ex.getMessage());
        }

        return fileList;
    }

    // this method will
    public List<FileVO> getFilesByDirectory(String directoryPath) {
        List<FileVO> fileList = new ArrayList<>();

        try {
            Path path = Paths.get(directoryPath);
            File dir = new File(String.valueOf(path));
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileVO fileVO = new FileVO();
                    fileVO.setFilename(file.getName());
                    fileVO.setDirectory(file.getParent()); // .\topdir\2011253\barcodes\all
                    fileList.add(fileVO);
                }
            } else {
                System.out.println("Error getting list of files, should never happen.");
            }
        } catch (Exception ex) {
            System.out.println("Exception in getFilesByDirectory");
            System.out.println("Caught exception: " + ex.getMessage());
        }

        return fileList;
    }



    public Page<FileVO> getPagedFilesByDirectory(String directoryPath, int page, int size) {

        if(page != 0){
            page = page - 1;
        }

        List<FileVO> fileList = getFilesByDirectory(directoryPath);
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), fileList.size());
        int end = Math.min((start + pageable.getPageSize()), fileList.size());
        List<FileVO> pagedFiles = fileList.subList(start, end);
        return new PageImpl<>(pagedFiles, pageable, fileList.size());
    }


    public String saveInvoiceToFileSystem(PDDocument document, TransactionVO transaction) throws IOException {

        // first cycle through the list of payments and see which one is the most recent
        PaymentVO payment = transaction.getMostRecentPayment();

        // this will make it save a new version of the invoice every time a payment is applied
        String filename = transaction.getCustomervo().getName().trim().replaceAll(" ","_")+transaction.getCustomervo().getCustomerid()+"_invoice_"+"payment_"+payment.getPaymentid();

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.getPARENT_LEVEL_DIR()+appConstants.getTRANSACTION_INVOICE_DIR()+transaction.getTransactionid()));

        // save the actual file
        document.save(appConstants.getPARENT_LEVEL_DIR()+appConstants.getTRANSACTION_INVOICE_DIR()+transaction.getTransactionid()+"/"+filename+".pdf");
        //document.close();
        return appConstants.getPARENT_LEVEL_DIR()+appConstants.getTRANSACTION_INVOICE_DIR()+transaction.getTransactionid()+"/"+filename+".pdf";
    }


    public String sanitizeMultiPartFileName(MultipartFile file) {
        String cleanName = file.getOriginalFilename().replaceAll("-","_");
        cleanName = file.getOriginalFilename().replaceAll(" ","_");
        cleanName = file.getOriginalFilename().replaceAll("\\.","_");
        cleanName = StringUtils.cleanPath(file.getOriginalFilename());
        return cleanName;
    }


}
