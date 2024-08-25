package com.techvvs.inventory.runlistener;

import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.refdata.RefDataLoader;
import com.techvvs.inventory.util.SimpleCache;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class GlobalRunListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    SimpleCache simpleCache;

    @Autowired
    RefDataLoader refDataLoader;

    @Autowired
    Environment environment;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("----- TechVVS Application has started ----");
        System.out.println("------- TechVVS Custom Cache Init ------");


       // linuxDataUtil.analyzejson();

        if(environment.getProperty("load.ref.data").equals("yes")){
            refDataLoader.loadRefData();
        }


        //encryptUtil.generateAESKey();


//        StringBuilder sb = new StringBuilder();
//        // pull all the products and put the barcodes in a list
//        List<ProductVO> products = productRepo.findAll();


//        try {
//            writeProductsToExcel(products,
//                    appConstants.getPARENT_LEVEL_DIR()+appConstants.getBARCODES_TRANSFER_DIR(),
//                    appConstants.getPARENT_LEVEL_DIR()+appConstants.getBARCODES_TRANSFER_DIR()+"/transferadhoc.xlsx"
//
//            );
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        //     printerService.printReceipt("Hello World");
  //      xlsxImporter.testImportXLSXFile();

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


    void writeProductsToExcel(List<ProductVO> products, String dirpath,String filePath) throws IOException {
        // Create a workbook and a sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // Create the header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("name");
        headerRow.createCell(1).setCellValue("barcode");


        // Iterate through the list of products and add them to the sheet
        int index = 1; // Start from the second row
        for (ProductVO product : products) {
            Row dataRow = sheet.createRow(index);
            dataRow.createCell(0).setCellValue(product.getName());
            dataRow.createCell(1).setCellValue(product.getBarcode());
            index++;
        }



        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(dirpath));

        // Write the workbook to a file
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

    }





}
