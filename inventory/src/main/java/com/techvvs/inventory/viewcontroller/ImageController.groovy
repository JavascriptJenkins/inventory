package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

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
            @PathVariable Optional<String> token

    ) {

        Path file = Paths.get(appConstants.UPLOAD_DIR+"media/product/"+productid+"/primaryphoto").resolve("primary.jpg");
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }


}
