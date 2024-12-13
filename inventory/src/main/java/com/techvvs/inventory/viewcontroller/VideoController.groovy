package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
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
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import java.nio.file.Path
import java.nio.file.Paths

@RequestMapping("/video")
@Controller
class VideoController {

    @Autowired
    AppConstants appConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    ProductRepo productRepo

    @GetMapping("/videos/{productid}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String productid,
            @PathVariable String filename,
            @PathVariable Optional<String> token

    ) {

        Path file = Paths.get(appConstants.UPLOAD_DIR+"media/product/"+productid+"/videos").resolve(filename);
        Resource resource = new FileSystemResource(file);

        if(filename.contains(".mp4")) {
            return ResponseEntity.ok().contentType(MediaType.valueOf("video/mp4")).body(resource);
        }
        if(filename.contains(".mov")) {
            return ResponseEntity.ok().contentType(MediaType.valueOf("video/quicktime")).body(resource);
        }
        return ResponseEntity.ok().contentType(MediaType.valueOf("video/mp4")).body(resource);

    }

    // this serves the default menu for the batch
    @GetMapping("/product")
    String getVideoListForProduct(
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("page") Optional<String> page,
            @RequestParam("size") Optional<String> size
    ){

        // this needs to get a list of FileVO's that are populated with info from the video folder
        ProductVO productVO = productRepo.findById(Integer.valueOf(productid.get())).get(

        )

        Page<FileVO> filePage = filePagingService.getFilePageForProductUploadMedia(productVO, Integer.valueOf(page.orElse("0")), Integer.valueOf(size.orElse("5")), Paths.get(appConstants.UPLOAD_DIR_MEDIA+appConstants.UPLOAD_DIR_PRODUCT+productid.get()+appConstants.UPLOAD_DIR_PRODUCT_VIDEOS).toString())

        if(filePage.size() > 0){
            model.addAttribute("filePage", filePage);
        } else {
            model.addAttribute("filePage", null);
        }

        model.addAttribute("menuid", menuid.orElse("0"));// bind this for the back button

        // fetch all customers from database and bind them to model
        //checkoutHelper.getAllCustomers(model)
        //techvvsAuthService.checkuserauth(model)
        return "menu/videoplayer.html";
    }


}
