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
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.qrcode.QrCodeService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.util.FormattingUtil
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

import java.nio.file.Paths
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
    FormattingUtil formattingUtil

    @Autowired
    QrCodeService qrCodeService

    @Autowired
    LabelPrintingService labelPrintingService

    @Autowired
    ControllerConstants controllerConstants

    @Autowired
    TechvvsAuthService techvvsAuthService

    SecureRandom secureRandom = new SecureRandom();



    boolean generateSingleMenuBarcodesForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

      //  barcodeService.createSingleMenuBarcodesForBatch(results.get(0))

        return true
    }

    // will generate a barcode for every single product in the batch
    boolean generateAllBarcodesForBatch(String batchnumber){

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
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
                       String batchnumber,
                       String editmode,
                       Optional<Integer> page,
                       ProductTypeVO productTypeVO,
                       boolean isFilteredView,
                       boolean isProductNameLikeSearch

    ){

        // bind the common elements to the batch and return the parent batchVO object
        BatchVO batchVO =bindCommonElements(model, batchnumber, editmode);

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

        int quantityRemaining = 0;
        int quantity = 0;
        Double batchValueTotal = 0;
        Double batchValueRemainingTotal = 0;
        int quantityRemainingInCarts = 0;
        int quantityInTransactions = 0;
        int quantityInPaidTransactions = 0;

        for(ProductVO productVO : batchVO.product_set){

            if(productVO.price == null){
                productVO.price = 0.00
            }

            if(productVO.cost == null){
                productVO.cost = 0.00
            }

            if(productVO.quantityremaining == null){
                productVO.quantityremaining = 0
            }



            // we only want to run the cart check once per instance cart id
            Set<Integer> seenCarts = new HashSet<>();

            // cycle thru each cart the product is associated with and count the instances
            for (CartVO cartVO : productVO.cart_list) {
                if(!seenCarts.contains(cartVO.cartid)) {
                    if (cartVO.isprocessed == 0) {
                        for (ProductVO cartProductVO : cartVO.product_cart_list) {
                            quantityRemainingInCarts = (productVO.product_id == cartProductVO.product_id && cartVO.isprocessed == 0) ? quantityRemainingInCarts + 1 : quantityRemainingInCarts
                        }
                    }
                }
                seenCarts.add(cartVO.cartid);
             }



            String previousbarcode = ""

            for(TransactionVO transactionVO : productVO.transaction_list){
                for(ProductVO transactionProductVO : transactionVO.product_list){
                    if(previousbarcode.equals(transactionProductVO.barcode)){
                        // skip
                    } else {
                        quantityInTransactions = (productVO.product_id == transactionProductVO.product_id && transactionVO.paid < transactionVO.totalwithtax) ? quantityInTransactions + 1 : quantityInTransactions
                        quantityInPaidTransactions = (productVO.product_id == transactionProductVO.product_id && transactionVO.paid >= transactionVO.totalwithtax) ? quantityInPaidTransactions + 1 : quantityInPaidTransactions
                    }
                    previousbarcode = transactionProductVO.barcode
                }
            }


            quantityRemaining = productVO.quantityremaining + quantityRemaining
            quantity = productVO.quantity + quantity
            batchValueTotal = (Double.valueOf(productVO?.price) * Integer.valueOf(productVO.quantity)) + batchValueTotal
            batchValueRemainingTotal = (Double.valueOf(productVO.price) * productVO.quantityremaining) + batchValueRemainingTotal
        }


        model.addAttribute("quantity", quantity)
        model.addAttribute("quantityRemaining", quantityRemaining)
        model.addAttribute("quantityRemainingInCarts", quantityRemainingInCarts)
        model.addAttribute("quantityInTransactions", quantityInTransactions)//
        model.addAttribute("quantityInPaidTransactions", quantityInPaidTransactions)//
        model.addAttribute("batchValueTotal", batchValueTotal)
        model.addAttribute("batchValueRemainingTotal", batchValueRemainingTotal)
    }


    BatchVO bindCommonElements(Model model, String batchnumber, String editmode){

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
        model.addAttribute(controllerConstants.MENU_OPTIONS_TEXT_XLSX, [0.00, 0.50, 100.00, 150.00, 200.00, 250.00, 300.00, -0.50, -100.00, -150.00, -200.00, -250.00, -300.00]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_BARCODE, ["All", "Single Menu"]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_QR_CODES, ["All", "Single Menu"]);
        model.addAttribute(controllerConstants.MENU_OPTIONS_WEIGHT_LABELS, [controllerConstants.SINGLE_PAGE]);
        techvvsAuthService.checkuserauth(model);
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


    //String UPLOAD_DIR = "./uploads/menus/";

    boolean sendTextMessageWithDownloadLink(Model model, String username, String batchnumber, Double priceadjustment){

        BatchVO batchVO = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber))?.get(0)

        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(username)


        if(batchVO != null && systemUserDAO != null){
            // create the file and put it in /batch/pricesheets/

            String datetime = formattingUtil.getDateTimeForFileSystem()

            String filename = createExcelFile(appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+appConstants.BATCH_PRICE_SHEETS_DIR+batchVO.name+"_"+datetime+"_pa_"+String.valueOf(priceadjustment)+".xlsx", batchVO, priceadjustment)

            boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"));

            // SystemUserDAO systemUserDAO, String token, boolean isDev1
            // send a text message with a download link
            try{
                textUtil.actuallySendOutDownloadLinkWithToken(filename, systemUserDAO, isDev1)
            } catch(Exception ex){
                System.out.println("Caught Exception: "+ex.getMessage())
            } finally {
                return true
            }

        }



    }


    String createExcelFile(String filename, BatchVO batchVO, Double priceadjustment){

        // Create a workbook and a sheet
        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet("SampleSheet")

        // NOTE: default column width is 2048. (256*8)
        // TODO: write code to set column width based on character size

        // set the column header names
        Row row = sheet.createRow(0)
        row.createCell(0).setCellValue("Quantity")
        sheet.setColumnWidth(1, 10000); // Set width of the first column for flavor
        row.createCell(1).setCellValue("Flavor")
        row.createCell(2).setCellValue("Ticket")
        sheet.setColumnWidth(3, 10000); // Set width of the first column for barcode
        row.createCell(3).setCellValue("Barcode")

        // set the values
        for(int i=1;i<batchVO.product_set.size()+1;i++){
            row = sheet.createRow(i)
            ProductVO productVO = batchVO.product_set[i - 1] // need to subtract 1 here to account for header row at index 0
        //    setColumnWidthBasedOnString(sheet, 0 ,productVO.name) // set width of each name cell based on length
            row.createCell(0).setCellValue(productVO.quantityremaining)
            row.createCell(1).setCellValue(productVO.name)
            row.createCell(2).setCellValue(productVO.price + priceadjustment)
            row.createCell(3).setCellValue(productVO.barcode)


        }


        // create the directory if it doesn't exist
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+appConstants.BATCH_PRICE_SHEETS_DIR))

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

        int batchNumber = generateIntBatchNumber()

        // Loop until a unique batch number is generated
        batchNumber = generateIntBatchNumber()
//        while (batchRepo.existsByBatchnumber(batchNumber)) {
//            batchNumber = generateIntBatchNumber()
//        }




        return batchNumber.toString(); // cast to a string so it can be inserted in excel cells
    }

    int generateIntBatchNumber(){
        int length = 7; // Set the length to 8 digits
        StringBuilder batchNumber = new StringBuilder(length);

        // Ensure the first digit is non-zero
        batchNumber.append(secureRandom.nextInt(9) + 1);

        // Append remaining digits
        for (int i = 1; i < length; i++) {
            batchNumber.append(secureRandom.nextInt(10));
        }
        return batchNumber.toInteger();
    }


    BatchVO loadBatch(String batchid, Model model){

        BatchVO batchVO = new BatchVO()
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(batchid == "0"){
            // do nothing
            // if it is the first time loading the page

            model.addAttribute("batch", batchVO);
            return batchVO

        } else {
            batchVO = getExistingBatch(batchid)

            model.addAttribute("batch", batchVO);
            return batchVO
        }

    }


    BatchVO getExistingBatch(String batchid){

        Optional<BatchVO> batchVO = batchRepo.findById(Integer.valueOf(batchid))

        if(!batchVO.empty){
            return batchVO.get()
        } else {
            return new BatchVO(batchid: 0)
        }
    }

}
