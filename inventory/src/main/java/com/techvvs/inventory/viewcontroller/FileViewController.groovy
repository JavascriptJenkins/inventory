package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.modelnonpersist.MenuOptionVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.PaymentService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.FileViewHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
import com.techvvs.inventory.viewcontroller.helper.TransactionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom

@RequestMapping("/viewfiles")
@Controller
public class FileViewController {


    @Autowired
    AppConstants appConstants

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    TechvvsFileHelper techvvsFileHelper;

    @Autowired
    ControllerConstants controllerConstants

    @Autowired
    FilePagingService filePagingService

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    BatchDao batchDao;


    @Autowired
    BatchTypeRepo batchTypeRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;


    @Autowired
    ProductRepo productRepo;


    @Autowired
    ValidateBatch validateBatch;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    FileViewHelper fileViewHelper

    @Autowired
    TransactionHelper transactionHelper

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    MenuHelper menuHelper

    @Autowired
    CartService cartService

    @Autowired
    TransactionService transactionService

    @Autowired
    PrinterService printerService

    @Autowired
    PaymentHelper paymentHelper



    @Autowired
    PaymentService paymentService


    SecureRandom secureRandom = new SecureRandom();


    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "batch" ) BatchVO batchVO,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("batchid") String batchid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        batchid = batchid == null ? "0" : String.valueOf(batchid)

        // attach the paymentVO to the model
        batchVO = batchControllerHelper.loadBatch(batchid, model)


        // start file paging
        String dir = appConstants.PARENT_LEVEL_DIR+String.valueOf(batchVO.batchnumber)+appConstants.BARCODES_ALL_DIR
        Page<FileVO> filePage = filePagingService.getFilePageFromDirectory(page.get(), size.get(), dir)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        // end file paging



        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, [appConstants.BARCODES_ALL_DIR, appConstants.BARCODES_MENU_DIR, appConstants.QR_ALL_DIR,]);
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("menuoption", new MenuOptionVO(selected: ""));
        return "files/batchfiles.html";
    }


    @PostMapping("/viewdirfiles")
    String submitpayment(
            @ModelAttribute( "batch" ) BatchVO batchVO,
            @RequestParam( "batchid" ) String batchid,
            @ModelAttribute( "menuoption" ) MenuOptionVO menuoption,
            Model model,
            @RequestParam("customJwtParameter") String customJwtParameter,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){



        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);

        batchid = batchid == null ? "0" : String.valueOf(batchid)
        batchVO = batchControllerHelper.loadBatch(batchid, model)

        String selected = menuoption.selected // this contains file path from the dropdown

        Page<FileVO> filePage = filePagingService.getFilePage(batchVO, page, size, selected)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, [appConstants.BARCODES_ALL_DIR, appConstants.BARCODES_MENU_DIR, appConstants.QR_ALL_DIR]);
        model.addAttribute("customJwtParameter", customJwtParameter);


        return "files/batchfiles.html";
    }



    @PostMapping("/textemailinvoice")
    String textemailinvoice(
                @RequestParam( "batchid" ) String batchid,
                            @ModelAttribute( "menuoption" ) MenuOptionVO menuoption,
                            Model model,
                            @RequestParam("customJwtParameter") String customJwtParameter,
                            @RequestParam("page") Optional<Integer> page,
                            @RequestParam("size") Optional<Integer> size){

        System.out.println("customJwtParam on checkout controller: "+customJwtParameter);

        menuoption = fileViewHelper.sanitizeTransients(menuoption)

        batchid = batchid == null ? "0" : String.valueOf(batchid)
        BatchVO batchVO = batchControllerHelper.loadBatch(batchid, model)

        String selected = menuoption.selected // this contains file path from the dropdown
        String filename = menuoption.filenametosend // this contains file path from the dropdown



        // now here we need to actually send out the file
        String dir = appConstants.PARENT_LEVEL_DIR+String.valueOf(batchVO.batchnumber)+selected+"/"+filename

        if (menuoption.action.contains(appConstants.TEXT_INVOICE)) {
            transactionHelper.sendTextMessageWithDownloadLink(menuoption.phonenumber, dir)
        } else if (menuoption.action.contains(appConstants.EMAIL_INVOICE)) {
            transactionHelper.sendEmailWithDownloadLink(menuoption.email, dir)
        }



        // start file paging
        Page<FileVO> filePage = filePagingService.getFilePage(batchVO, page, size, selected)
        filePagingService.bindPageAttributesToModel(model, filePage, page, size);
        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, [appConstants.BARCODES_ALL_DIR, appConstants.BARCODES_MENU_DIR, appConstants.QR_ALL_DIR]);
        // end file paging

        model.addAttribute("customJwtParameter", customJwtParameter);

        if(model.getAttribute("errorMessage") == null){
            model.addAttribute("successMessage", "Successfully sent file: "+filename+
                    "using method: "+menuoption.action)

            return "files/batchfiles.html";

        } else {
            return "files/batchfiles.html";
        }

    }



}
