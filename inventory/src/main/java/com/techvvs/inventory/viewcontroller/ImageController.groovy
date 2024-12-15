package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
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

@RequestMapping("/image")
@Controller
class ImageController {

    @Autowired
    AppConstants appConstants

    // todo: enforce token security on this for fetching the images.  otherwise people could get all our images
    @GetMapping("/images/{productid}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String productid,
            @PathVariable Optional<String> token) {

        // Define the possible file types
        Path jpgFile = Paths.get(appConstants.UPLOAD_DIR + "media/product/" + productid + "/primaryphoto").resolve("primary.jpg");
        Path pngFile = Paths.get(appConstants.UPLOAD_DIR + "media/product/" + productid + "/primaryphoto").resolve("primary.png");

        Resource resource;

        // Check for the existence of the file
        if (Files.exists(jpgFile)) {
            resource = new FileSystemResource(jpgFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } else if (Files.exists(pngFile)) {
            resource = new FileSystemResource(pngFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } else {
            // Return 404 if neither file exists
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



}
