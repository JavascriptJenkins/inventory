package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

@RequestMapping("/image")
@Controller
class ImageController {

    @Autowired
    AppConstants appConstants

    @GetMapping("/images/{productid}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String productid,
            @PathVariable Optional<String> token) {

        // Define the possible file types
        Path jpgFile = Paths.get(appConstants.UPLOAD_DIR + "media/product/" + productid + "/primaryphoto").resolve("primary.jpg");
        Path pngFile = Paths.get(appConstants.UPLOAD_DIR + "media/product/" + productid + "/primaryphoto").resolve("primary.png");

        Resource resource;
        MediaType mediaType;
        long lastModifiedTime;

        try {
            if (Files.exists(jpgFile)) {
                resource = new FileSystemResource(jpgFile);
                mediaType = MediaType.IMAGE_JPEG;
                lastModifiedTime = Files.getLastModifiedTime(jpgFile).toMillis();
            } else if (Files.exists(pngFile)) {
                resource = new FileSystemResource(pngFile);
                mediaType = MediaType.IMAGE_PNG;
                lastModifiedTime = Files.getLastModifiedTime(pngFile).toMillis();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Add caching headers
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        headers.setLastModified(lastModifiedTime);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(resource);
    }




}
