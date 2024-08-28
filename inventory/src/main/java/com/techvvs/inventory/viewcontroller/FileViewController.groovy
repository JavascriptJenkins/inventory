package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.modelnonpersist.MenuOptionVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.FileViewHelper
import com.techvvs.inventory.viewcontroller.helper.TransactionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/viewfiles")
@Controller
public class FileViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    TechvvsFileHelper techvvsFileHelper

    @Autowired
    ControllerConstants controllerConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    FileViewHelper fileViewHelper

    @Autowired
    TransactionHelper transactionHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService




    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
//            @RequestParam("selected") String selected,
    @ModelAttribute( "menuoption" ) MenuOptionVO menuoption
    ){

        batchid = batchid == null ? "0" : String.valueOf(batchid)

        // attach the paymentVO to the model
         BatchVO batchVO = batchControllerHelper.loadBatch(batchid, model)


        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+String.valueOf(batchVO.batchnumber)+appConstants.BARCODES_ALL_DIR

//        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        Page<FileVO> filePage = filePagingService.getFilePage(batchVO, page.get(), size.get(), '/'+menuoption.selected+'/')
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging



        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, controllerConstants.DIRECTORIES_FOR_BATCH_UI);
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("menuoption", new MenuOptionVO(selected: '/'+menuoption.selected+'/'));
        return "files/batchfiles.html";
    }


    @PostMapping("/viewdirfiles")
    String viewdirfiles(
            @RequestParam( "batchid" ) String batchid,
            @ModelAttribute( "menuoption" ) MenuOptionVO menuoption,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){



        

        batchid = batchid == null ? "0" : String.valueOf(batchid)
        BatchVO batchVO = batchControllerHelper.loadBatch(batchid, model)

        String selected = menuoption.selected // this contains file path from the dropdown

        Page<FileVO> filePage = filePagingService.getFilePage(batchVO, page.get(), size.get(), selected)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, controllerConstants.DIRECTORIES_FOR_BATCH_UI);
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("menuoption", new MenuOptionVO(selected: selected));

        return "files/batchfiles.html";
    }



    @PostMapping("/textemailinvoice")
    String textemailinvoice(
                @RequestParam( "batchid" ) String batchid,
                            @ModelAttribute( "menuoption" ) MenuOptionVO menuoption,
                            Model model,
                            @RequestParam("page") Optional<Integer> page,
                            @RequestParam("size") Optional<Integer> size){

        

        menuoption = fileViewHelper.sanitizeTransients(menuoption)

        batchid = batchid == null ? "0" : String.valueOf(batchid)
        BatchVO batchVO = batchControllerHelper.loadBatch(batchid, model)

        String selected = menuoption.selected // this contains file path from the dropdown
        String filename = menuoption.filenametosend // this contains file path from the dropdown


        filename = filename.replaceAll(",", "")
        String contentsofinvoice = ""
        // now here we need to actually send out the file
        String dir = appConstants.PARENT_LEVEL_DIR+String.valueOf(batchVO.batchnumber)+selected+"/"+filename

        if (menuoption.action.contains(appConstants.TEXT_INVOICE)) {
            transactionHelper.sendTextMessageWithDownloadLink(menuoption.phonenumber, dir)
        } else if (menuoption.action.contains(appConstants.EMAIL_INVOICE)) {
            transactionHelper.sendEmailWithDownloadLink(menuoption.email, dir)
        } else if (menuoption.action.contains(appConstants.VIEW_INVOICE)) {
            contentsofinvoice = techvvsFileHelper.readPdfAsBase64String(dir)
        }



        // start file paging
        Page<FileVO> filePage = filePagingService.getFilePage(batchVO, page.get(), size.get(), selected)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, controllerConstants.DIRECTORIES_FOR_BATCH_UI);
        // end file paging

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("menuoption", new MenuOptionVO(selected: selected));
        model.addAttribute("invoicecontent", contentsofinvoice)

        // only send a successmessage if we sent out an email or text, viewing file will get no successmessage
        if(model.getAttribute("errorMessage") == null && contentsofinvoice.length() == 0){
            model.addAttribute("successMessage", "Successfully sent file: "+filename+
                    "using method: "+menuoption.action)

            return "files/batchfiles.html";

        } else {
            return "files/batchfiles.html";
        }

    }



}
