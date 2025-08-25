package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.service.paging.FilePagingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.http.HttpStatus;


import java.nio.file.Path
import java.nio.file.Paths

@RequestMapping("/photo")
@Controller
class PhotoController {

    @Autowired
    AppConstants appConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    ProductRepo productRepo

    @GetMapping("/photos/{productid}/{filename}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String productid,
            @PathVariable String filename,
            @RequestParam Optional<String> token // Use @RequestParam for query parameters
    ) {
        // Construct the file path
        Path file = Paths.get(appConstants.UPLOAD_DIR + "media/product/" + productid + "/photos").resolve(filename);
        Resource resource = new FileSystemResource(file);

        // Determine the content type
        String lowerCaseFilename = filename.toLowerCase();
        MediaType contentType;

        if (lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")) {
            contentType = MediaType.IMAGE_JPEG;
        } else if (lowerCaseFilename.endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG;
        } else {
            // Return a 415 Unsupported Media Type response for unsupported file types
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        // Return the file as a response
        return ResponseEntity.ok()
                .contentType(contentType)
                .body(resource);
    }


    // this serves the default menu for the batch
    @GetMapping("/product")
    String getVideoListForProduct(
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken,
            @RequestParam("page") Optional<String> page,
            @RequestParam("size") Optional<String> size
    ){

        // this needs to get a list of FileVO's that are populated with info from the video folder
        ProductVO productVO = productRepo.findById(Integer.valueOf(productid.get())).get(

        )

        Page<FileVO> filePage = filePagingService.getFilePageForProductUploadMedia(productVO, Integer.valueOf(page.orElse("0")), Integer.valueOf(size.orElse("5")), Paths.get(appConstants.UPLOAD_DIR_MEDIA+appConstants.UPLOAD_DIR_PRODUCT+productid.get()+appConstants.UPLOAD_DIR_PRODUCT_PHOTOS).toString())

        if(filePage.size() > 0){
            model.addAttribute("filePage", filePage);
        } else {
            model.addAttribute("filePage", null);
        }

        model.addAttribute("menuid", menuid.orElse("0"));// bind this for the back button
        model.addAttribute("productid", productid.orElse("0"));// bind this for the back button
        model.addAttribute("shoppingtoken", shoppingtoken.orElse("0"));// bind this for the back button

        // fetch all customers from database and bind them to model
        //checkoutHelper.getAllCustomers(model)
        //techvvsAuthService.checkuserauth(model)
        return "menu5/photoplayer.html";
    }


}
