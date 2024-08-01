package com.techvvs.inventory.util;

import com.techvvs.inventory.modelnonpersist.FileVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class TechvvsFileHelper {


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
        List<FileVO> fileList = getFilesByDirectory(directoryPath);
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset() - size, fileList.size());
        int end = Math.min((start + pageable.getPageSize()), fileList.size());
        List<FileVO> pagedFiles = fileList.subList(start, end);
        return new PageImpl<>(pagedFiles, pageable, fileList.size());
    }


}
