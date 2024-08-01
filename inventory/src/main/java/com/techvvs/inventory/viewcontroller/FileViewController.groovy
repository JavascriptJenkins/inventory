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
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import com.techvvs.inventory.viewcontroller.helper.PaymentHelper
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

        // attach the filelist to the model
        Page<FileVO> filePage = techvvsFileHelper.getPagedFilesByDirectory(
                appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+appConstants.BARCODES_ALL_DIR,
                page.get(),
                size.get()
        );

        bindPageAttributesToModel(model, filePage, page, size);
        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, [appConstants.BARCODES_ALL_DIR, appConstants.BARCODES_MENU_DIR]);
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

        // attach the filelist to the model
        Page<FileVO> filePage = techvvsFileHelper.getPagedFilesByDirectory(
                appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+selected,
                page.get(),
                size.get()
        );

        // need to pass the original size of all files into this
        bindPageAttributesToModel(model, filePage, page, size);
        model.addAttribute(controllerConstants.MENU_OPTIONS_DIRECTORIES, [appConstants.BARCODES_ALL_DIR, appConstants.BARCODES_MENU_DIR]);
        model.addAttribute("customJwtParameter", customJwtParameter);


        return "files/batchfiles.html";
    }



    void bindPageAttributesToModel(Model model, Page<FileVO> filePage, Optional<Integer> page, Optional<Integer> size) {


        int currentPage = page.orElse(0);
        int totalPages = filePage.getTotalPages(); // for some reason there needs to be a -1 here ... dont ask ...

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", filePage.getTotalPages());
        model.addAttribute("filePage", filePage);
    }



}
