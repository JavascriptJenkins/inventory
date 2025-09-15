package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.labels.service.LabelPrintingService
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.modelnonpersist.LockVO
import com.techvvs.inventory.modelnonpersist.MenuOptionVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.lock.LockService
import com.techvvs.inventory.service.paging.FilePagingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@RequestMapping("/label")
@Controller
public class LabelViewController {


    @Autowired
    LockService lockService

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    LabelPrintingService labelPrintingService

    @Autowired
    FilePagingService filePagingService

    @Autowired
    AppConstants appConstants


    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        String dir = appConstants.PARENT_LEVEL_DIR+appConstants.LABEL_DIR+0+appConstants.ADHOC_DIR

        model.addAttribute("menu", new MenuVO())

        // start file paging
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.orElse(0), size.orElse(5), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging
        techvvsAuthService.checkuserauth(model)
        return "label/label.html";
    }


    @PostMapping("/printadhoc")
    String printadhoc(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            @ModelAttribute( "menuoption" ) MenuOptionVO menuoption,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            Model model
    ){
        String dironly = appConstants.PARENT_LEVEL_DIR+appConstants.LABEL_DIR+0+appConstants.ADHOC_DIR+"/"
        validateMenuAdhoc(menuVO, model)

        if(model.getAttribute("errorMessage") == null){
            // get the
            labelPrintingService.printAdhocLabelSheet(menuVO)
            model.addAttribute("successMessage", "generated adhoc label for: "+menuVO.adhoc_label1 + "|" + menuVO.adhoc_label2 + "|" + menuVO.adhoc_label3)
        }

        techvvsAuthService.checkuserauth(model)
        // start file paging
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.orElse(0), size.orElse(5), dironly)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging

        return "label/label.html";
    }

    @PostMapping("/unlockdata")
    String unlockdata(
            @ModelAttribute( "lock" ) LockVO lockVO,
            Model model
    ){

        lockService.unlockdata(lockVO)

        boolean locked = lockService.checkifdataislocked()
        locked ? model.addAttribute("locked", "locked") : model.addAttribute("locked", "unlocked")

        model.addAttribute("successMessage", "Data unlocked successfully.  Don't loose your key or it will be gone forever. ")
        model.addAttribute("lock", new LockVO())
        techvvsAuthService.checkuserauth(model)

        return "lock/lock.html";
    }


    @PostMapping("/generatekey")
    String generatekey(
            @ModelAttribute( "lock" ) LockVO lockVO,
            Model model
    ){

        // Slu/m9UwOfIjrAsOQQRftF41x4WdVppzHSU8PIJ6SPU=

        String key = lockService.generateSecretKey()
        boolean locked = lockService.checkifdataislocked()

        locked ? model.addAttribute("locked", "locked") : model.addAttribute("locked", "unlocked")

        model.addAttribute("successMessage", "Don't loose your key or your data will be gone FOREVER. ")
        model.addAttribute("lock", new LockVO(secretkey: key))
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("keygenerated", "true");

        return "lock/lock.html";
    }


    void validateMenuAdhoc(MenuVO menuVO, Model model){
        if(
                menuVO.adhoc_label1.length() > 100 || menuVO.adhoc_label1.length() < 1 ||
                menuVO.adhoc_label2.length() > 100 || menuVO.adhoc_label2.length() < 0 ||
                menuVO.adhoc_label3.length() > 100 || menuVO.adhoc_label3.length() < 0
        ){
            model.addAttribute("errorMessage", "Adhoc labels must be between 0 and 100 characters")
        }
    }




}
