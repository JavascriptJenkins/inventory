package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.labels.service.LabelPrintingService
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.qrcode.QrCodeService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.util.TwilioTextUtil
import com.techvvs.inventory.viewcontroller.constants.ControllerConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.nio.file.Files
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.*

import java.security.SecureRandom

/* This class exists to keep the main batch controller code clean and avoid duplication. */
@Component
class BatchControllerHelper {

    @Autowired
    TechvvsFileHelper techvvsFileHelper

    @Autowired
    BatchTypeRepo batchTypeRepo

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductRepo productRepo

    @Autowired
    ProductTypeRepo productTypeRepo

    @Autowired
    BatchRepo batchRepo

    @Autowired
    TwilioTextUtil textUtil

    @Autowired
    Environment env

    @Autowired
    SystemUserRepo systemUserRepo

    @Autowired
    BarcodeService barcodeService

    @Autowired
    QrCodeService qrCodeService

    @Autowired
    LabelPrintingService labelPrintingService

    @Autowired
    ControllerConstants controllerConstants

    SecureRandom secureRandom = new SecureRandom();



    boolean generateSingleMenuBarcodesForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        barcodeService.createSingleMenuBarcodesForBatch(results.get(0))

        return true
    }

    // will generate a barcode for every single product in the batch
    boolean generateAllBarcodesForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        if(results.get(0).product_set.getAt(0).barcode != null && results.get(0).product_set.getAt(0).barcode != ""){
            // if one of the products has a barcode in this batch we are going to assume that
            // we already have barcodes for this batch
            return false
        }

        barcodeService.createAllBarcodesForBatch(results.get(0))

        return true
    }

    boolean generateQrcodesForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        qrCodeService.createSingleMenuQrsForBatch(results.get(0))

        return true
    }

    boolean generateAllQrcodesForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        qrCodeService.createAllQrsForBatch(results.get(0))

        return true
    }

    boolean generateSinglePageWeightLabelsForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        labelPrintingService.generate50StaticWeightLabels(results.get(0))

        return true
    }


    // method for combining logic of editform and filtereditform
    Model processModel(Model model,
                       String customJwtParameter,
                       String batchnumber,
                       String editmode,
                       Optional<Integer> page,
                       ProductTypeVO productTypeVO,
                       boolean isFilteredView,
                       boolean isProductNameLikeSearch

    ){

        // bind the common elements to the batch and return the parent batchVO object
        BatchVO batchVO =bindCommonElements(model, customJwtParameter, batchnumber, editmode);

        boolean hasProductTypeId = productTypeVO?.producttypeid != null

        if (isFilteredView) {
            if (isProductNameLikeSearch) {
                bindFilterProductsLikeSearch(model, page, productTypeVO.name, productTypeVO, batchVO);
            } else if (hasProductTypeId) {
                bindFilterProducts(model, page, productTypeVO, batchVO);
            } else {
                bindProducts(model, page, productTypeVO, batchVO);
            }
            model.addAttribute("searchproducttype", productTypeVO); // use the existing object for submitting a search term
        } else {
            bindProducts(model, page, productTypeVO, batchVO);
            model.addAttribute("searchproducttype", new ProductTypeVO()); // create a new object for submitting a search term
        }

    }


    void calculateStaticPageData(Model model, BatchVO batchVO){

        // todo: map these id's better?
        int INDOOR_PRODUCT_TYPE_ID = 35;
        int totalIndoorQuantityRemaining = 0;
        int totalIndoorQuantity = 0;
        int batchValueTotal = 0;
        int batchValueRemainingTotal = 0;
        int totalCartQuantityRemaining = 0;
        int totalEdibleQuantityRemaining = 0;
        for(ProductVO productVO : batchVO.product_set){

            // todo: move this to database inserts instead
            // this is assuming quantity is not boxes of product but individual products
            if(productVO.price == null){
                productVO.price = "0"
            }

            if(productVO.cost == null){
                productVO.cost = 0
            }

            if(productVO.quantityremaining == null){
                productVO.quantityremaining = 0
            }


            // calculate indoor quantity
            if(INDOOR_PRODUCT_TYPE_ID == productVO.producttypeid.producttypeid){
                totalIndoorQuantityRemaining = productVO.quantity + totalIndoorQuantityRemaining
                totalIndoorQuantity = productVO.quantityremaining + totalIndoorQuantity
            }


            batchValueTotal = (Integer.valueOf(productVO?.cost) * Integer.valueOf(productVO.quantity)) + batchValueTotal
            batchValueRemainingTotal = (Integer.valueOf(productVO.cost) * productVO.quantityremaining) + batchValueRemainingTotal
        }


        model.addAttribute("totalIndoorQuantity", totalIndoorQuantity)
        model.addAttribute("totalIndoorQuantityRemaining", totalIndoorQuantityRemaining)
        model.addAttribute("batchValueTotal", batchValueTotal)
        model.addAttribute("batchValueRemainingTotal", batchValueRemainingTotal)

    }


    BatchVO bindCommonElements(Model model, String customJwtParameter, String batchnumber, String editmode){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        // check to see if there are files uploaded related to this batchnumber
        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(batchnumber), appConstants.UPLOAD_DIR);

        if(filelist.size() > 0){
            model.addAttribute("filelist", filelist);
        } else {
            model.addAttribute("filelist", null);
        }

        if("yes".equals(editmode)){
            model.addAttribute("editmode",editmode)
        } else {
            model.addAttribute("editmode","no")
        }



        model.addAttribute("options", ["5", "10", "20", "100", "1000"]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_INDOOR, ["All","Indoor"]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_BARCODE, ["All", "Single Menu"]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_QR_CODES, ["All", "Single Menu"]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_WEIGHT_LABELS, [controllerConstants.SINGLE_PAGE]);
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", results.get(0));
        bindBatchTypes(model)
        bindProductTypes(model)
        // once products are bound, add the static data
        calculateStaticPageData(model, results.get(0))
        return results.get(0)
    }


    void bindBatchTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<BatchTypeVO> batchTypeVOS = batchTypeRepo.findAll();
        model.addAttribute("batchtypes", batchTypeVOS);
    }

    void bindProductTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypelist", productTypeVOS);
    }

    void bindProducts(Model model, Optional<Integer> page,ProductTypeVO productTypeVO, BatchVO batchVO){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = productTypeVO?.pagesize == null ? 5 : productTypeVO.pagesize;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }


        // this needs to only find these based on their batchid
        Page<ProductVO> pageOfProduct = productRepo.findAllByBatch(batchVO,pageable);

        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("productPage", pageOfProduct);
    }

    void bindFilterProductsLikeSearch(Model model, Optional<Integer> page, String name, ProductTypeVO productTypeVO, BatchVO batchVO){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = productTypeVO?.pagesize == null ? 5 : productTypeVO.pagesize;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<ProductVO> pageOfProduct = productRepo.findAllByNameContainingAndBatch(name, batchVO, pageable);


        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("productPage", pageOfProduct);
    }


    void bindFilterProducts(Model model, Optional<Integer> page, ProductTypeVO productTypeVO, BatchVO batchVO){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = productTypeVO?.pagesize == null ? 5 : productTypeVO.pagesize;;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<ProductVO> pageOfProduct = productRepo.findAllByProducttypeidAndBatch(productTypeVO, batchVO, pageable);



        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("productPage", pageOfProduct);
    }


    String UPLOAD_DIR = "./uploads/menus/";

    void sendTextMessageWithDownloadLink(Model model, String username, String batchnumber){

        BatchVO batchVO = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber))?.get(0)

        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(username)


        if(batchVO != null && systemUserDAO != null){
            // create the file and put it in /uploads/menus
            String filename = createExcelFile(UPLOAD_DIR+batchVO.name+".xlsx", batchVO)

            boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));

            // SystemUserDAO systemUserDAO, String token, boolean isDev1
            // send a text message with a download link
            textUtil.actuallySendOutDownloadLinkWithToken(filename, systemUserDAO, isDev1)
        }



    }


    String createExcelFile(String filename, BatchVO batchVO) {

        // Create a workbook and a sheet
        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet("SampleSheet")

        // NOTE: default column width is 2048. (256*8)
        // TODO: write code to set column width based on character size

        // set the column header names
        Row row = sheet.createRow(0)
        row.createCell(0).setCellValue("Name")
        sheet.setColumnWidth(0, 10000); // Set width of the first column for name
        row.createCell(1).setCellValue("Price")
        row.createCell(2).setCellValue("Stock")

        // set the values
        for(int i=1;i<batchVO.product_set.size()+1;i++){
            row = sheet.createRow(i)
            ProductVO productVO = batchVO.product_set[i - 1] // need to subtract 1 here to account for header row at index 0
        //    setColumnWidthBasedOnString(sheet, 0 ,productVO.name) // set width of each name cell based on length
            row.createCell(0).setCellValue(productVO.name)
            row.createCell(1).setCellValue(productVO.cost)
            row.createCell(2).setCellValue(productVO.quantityremaining)


        }



        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(filename)
        workbook.write(fileOut)
        fileOut.close()
        workbook.close()

        println("Excel file '$filename' created successfully.")
        return filename
    }

    //todo: make this method auto set the width of each column by the character size
    public static void setColumnWidthBasedOnString(Sheet sheet, int columnIndex, String inputString) {
        int stringLength = inputString.length();
        int widthMultiplier = 256; // 1 character width = 256 units
        int additionalSpace = 2; // Add 2 characters worth of space for padding
        int adjustedWidth = (stringLength + additionalSpace) * widthMultiplier;

        sheet.setColumnWidth(columnIndex, adjustedWidth);
    }

    String generateBatchNumber() {
        int length = 7; // Set the length to 8 digits
        StringBuilder batchNumber = new StringBuilder(length);

        // Ensure the first digit is non-zero
        batchNumber.append(secureRandom.nextInt(9) + 1);

        // Append remaining digits
        for (int i = 1; i < length; i++) {
            batchNumber.append(secureRandom.nextInt(10));
        }

        return batchNumber.toString();
    }

}
