package com.techvvs.inventory.runlistener;

import com.techvvs.inventory.barcode.BarcodeGenerator;
import com.techvvs.inventory.jparepo.BatchRepo;
import com.techvvs.inventory.jparepo.BatchTypeRepo;
import com.techvvs.inventory.jparepo.ProductRepo;
import com.techvvs.inventory.jparepo.ProductTypeRepo;
import com.techvvs.inventory.model.BatchTypeVO;
import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.ProductTypeVO;
import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.refdata.RefDataLoader;
import com.techvvs.inventory.util.SimpleCache;
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@Component
public class GlobalRunListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    BarcodeGenerator barcodeGenerator;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    SimpleCache simpleCache;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    RefDataLoader refDataLoader;

    @Autowired
    BatchTypeRepo batchTypeRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;

    String UPLOAD_DIR = "./uploads/menus/";
    String IMPORT_DIR = "./uploads/import/";

    SecureRandom secureRandom = new SecureRandom();


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("----- TechVVS Application has started ----");
        System.out.println("------- TechVVS Custom Cache Init ------");

        refDataLoader.loadRefData();

        testImportXLSXFile();

        // todo: move this somewhere else
//        int currentPage = 0;
//        int pageSize = 5;
//        Pageable pageable = null;
//        if(currentPage == 0){
//            pageable = PageRequest.of(0 , pageSize);
//        }
//        BatchVO batchVO = new BatchVO();
//        batchVO.setBatchid(7);

     //   BatchVO batchVO1 = batchRepo.findByBatchid(7);
       // Page<ProductVO> products =productRepo.findAllByBatch(batchVO1, pageable);
//        productRepo.findByIdBatchId(7, pageable);
        //productRepo.findByIdBatchId(Long.valueOf(7));

         //  batchControllerHelper.sendTextMessageWithDownloadLink(null, "unwoundcracker@gmail.com", "4314013");
        //String filename = UPLOAD_DIR+"testfile"+".xlsx";
       // batchControllerHelper.createExcelFile(filename, batchVO1);


//        try {
//
//            // run it 8 times 8*50 = 400
//            for(int i = 0; i < 8; i++) {
//                barcodeGenerator.generateBarcodes(String.valueOf(i));
//            }
//
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        simpleCache.refreshCache();
    }


    void testImportXLSXFile(){

        String excelFilePath = IMPORT_DIR+"export-1"+".xlsx";



            try (InputStream inputStream = new FileInputStream(excelFilePath);
                 Workbook workbook = new XSSFWorkbook(inputStream)) {


                // create a batch record in the database
                BatchVO batchVO = createBatchRecord(excelFilePath);

                // Get the first sheet
                Sheet sheet = workbook.getSheetAt(0);

                // Iterate through rows
                for (Row row : sheet) {

                    // skip the first 2 rows because they contain metadata
                    if(row.getRowNum() == 0
                            || row.getRowNum() == 1
                    ){
                        continue;
                    }

                    // now take each row and create a product entry
                    ProductVO productVO = new ProductVO();
                    productVO.setQuantity((int) row.getCell(0).getNumericCellValue());
                    productVO.setQuantityremaining((int) row.getCell(0).getNumericCellValue());
                    productVO.setName(row.getCell(1).getStringCellValue());
                    productVO.setPrice(String.valueOf(row.getCell(2).getNumericCellValue()));
                    productVO.setProductnumber(secureRandom.nextInt(10000000)); // assuming this will be unique ...

                    Optional<ProductTypeVO> productTypeVO = productTypeRepo.findById(2);
//                    productVO.setProducttypeid(productTypeVO.get());


                    productVO.setCreateTimeStamp(LocalDateTime.now());
                    productVO.setUpdateTimeStamp(LocalDateTime.now());

////////
                    productVO.setBatch(batchVO);

                    // add the product to the database
                    ProductVO result = saveNewProduct(productVO);

                    result.setProducttypeid(productTypeVO.get());
                    result = productRepo.save(result);

                    // add the product to the batch
                    batchVO = addProductToBatch(batchVO,result);
///////

                //    ProductVO result = productRepo.save(productVO);

                  //  result.setBatch(batchVO); // assign the product to the batch


                    if(batchVO.getProduct_set() != null){
                        batchVO.getProduct_set().add(productVO);
                    } else {
                        batchVO.setProduct_set(new HashSet<>());
                        batchVO.getProduct_set().add(productVO);
                    }


                    batchRepo.save(batchVO);










                    // Iterate through cells
                    for (Cell cell : row) {

                        switch (cell.getCellType()) {
                            case STRING:
                                System.out.print(cell.getStringCellValue() + "\t");
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    System.out.print(cell.getDateCellValue() + "\t");
                                } else {
                                    System.out.print(cell.getNumericCellValue() + "\t");
                                }
                                break;
                            case BOOLEAN:
                                System.out.print(cell.getBooleanCellValue() + "\t");
                                break;
                            case FORMULA:
                                System.out.print(cell.getCellFormula() + "\t");
                                break;
                            default:
                                System.out.print("UNKNOWN\t");
                                break;
                        }
                    }
                    System.out.println();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    ProductVO saveNewProduct(ProductVO productVO) {

        // when creating a new processData entry, set the last attempt visit to now - this may change in future
        productVO.setCreateTimeStamp(LocalDateTime.now());
        productVO.setUpdateTimeStamp(LocalDateTime.now());

        //todo: add support for product types on the ui so we can save this product object
        ProductVO result = productRepo.save(productVO);


        return result;

    }


    BatchVO createBatchRecord(String nameOfBatch){


        Optional<BatchTypeVO> batchTypeVO = batchTypeRepo.findById(1);

        // set the batch data
        BatchVO batchVO = new BatchVO();
        batchVO.setName(nameOfBatch);
        batchVO.setDescription("default batch description"); // insert date imported into here?
        batchVO.setBatchid(7);
        batchVO.setBatchnumber(secureRandom.nextInt(10000000)); // assuming this will be unique ...
        batchVO.setBatch_type_id(batchTypeVO.get()); // assume we always have the ref data loaded

        // when creating a new processData entry, set the last attempt visit to now - this may change in future
        batchVO.setCreateTimeStamp(LocalDateTime.now());
        batchVO.setUpdateTimeStamp(LocalDateTime.now());


        // first save the batch then we will add products to it and save it again
        BatchVO result = batchRepo.save(batchVO); //

        return result;

    }

    void hydrateBatchWithProducts(BatchVO batchVO){





    }


    BatchVO addProductToBatch(BatchVO batchVO, ProductVO result){
        batchVO = batchRepo.findByBatchid(batchVO.getBatchid());
            batchVO.setUpdateTimeStamp(LocalDateTime.now());
            // this means a valid batch was found
            batchVO.getProduct_set().add(result); // add the product from database to the product set
            batchVO = batchRepo.save(batchVO);
            return batchVO;


    }



}
