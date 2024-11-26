package com.techvvs.inventory.util

import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletResponse

@Component
class HeaderUtil {


    void setFileReturnHeader(String filename, HttpServletResponse response){
        if (filename.endsWith(".pdf")) {
            response.setContentType("application/pdf");
        } else if (filename.endsWith(".xlsx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if (filename.endsWith(".xls")) {
            response.setContentType("application/vnd.ms-excel");
        } else if (filename.endsWith(".doc")) {
            response.setContentType("application/msword");
        } else if (filename.endsWith(".docx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if (filename.endsWith(".ppt")) {
            response.setContentType("application/vnd.ms-powerpoint");
        } else if (filename.endsWith(".pptx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        } else if (filename.endsWith(".txt")) {
            response.setContentType("text/plain");
        } else if (filename.endsWith(".csv")) {
            response.setContentType("text/csv");
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            response.setContentType("image/jpeg");
        } else if (filename.endsWith(".png")) {
            response.setContentType("image/png");
        } else if (filename.endsWith(".gif")) {
            response.setContentType("image/gif");
        } else if (filename.endsWith(".bmp")) {
            response.setContentType("image/bmp");
        } else if (filename.endsWith(".svg")) {
            response.setContentType("image/svg+xml");
        } else if (filename.endsWith(".mp4")) {
            response.setContentType("video/mp4");
        } else if (filename.endsWith(".mov") || filename.endsWith(".MOV")) {
            response.setContentType("video/quicktime");
        } else if (filename.endsWith(".avi")) {
            response.setContentType("video/x-msvideo");
        } else if (filename.endsWith(".mp3")) {
            response.setContentType("audio/mpeg");
        } else if (filename.endsWith(".wav")) {
            response.setContentType("audio/wav");
        } else if (filename.endsWith(".zip")) {
            response.setContentType("application/zip");
        } else if (filename.endsWith(".rar")) {
            response.setContentType("application/vnd.rar");
        } else if (filename.endsWith(".7z")) {
            response.setContentType("application/x-7z-compressed");
        } else if (filename.endsWith(".json")) {
            response.setContentType("application/json");
        } else if (filename.endsWith(".xml")) {
            response.setContentType("application/xml");
        } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
            response.setContentType("text/html");
        } else if (filename.endsWith(".css")) {
            response.setContentType("text/css");
        } else if (filename.endsWith(".js")) {
            response.setContentType("application/javascript");
        } else {
            response.setContentType("application/octet-stream"); // Default for unknown types
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

    }




}
