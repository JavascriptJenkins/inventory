package com.techvvs.inventory.runlistener;

import com.techvvs.inventory.barcode.impl.BarcodeGenerator;
import com.techvvs.inventory.barcode.service.BarcodeService;

import com.techvvs.inventory.printers.PrinterService;
import com.techvvs.inventory.refdata.RefDataLoader;
import com.techvvs.inventory.util.SimpleCache;
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper;
import com.techvvs.inventory.xlsx.XlsxImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GlobalRunListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    BarcodeGenerator barcodeGenerator;

    @Autowired
    BatchControllerHelper batchControllerHelper;

    @Autowired
    SimpleCache simpleCache;

    @Autowired
    RefDataLoader refDataLoader;

    @Autowired
    XlsxImporter xlsxImporter;

    @Autowired
    BarcodeService barcodeService;

    @Autowired
    PrinterService printerService;

    String UPLOAD_DIR = "./uploads/menus/";
    String IMPORT_DIR = "./uploads/import/";

    SecureRandom secureRandom = new SecureRandom();


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("----- TechVVS Application has started ----");
        System.out.println("------- TechVVS Custom Cache Init ------");

        refDataLoader.loadRefData();



        printerService.print("Hello World");
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






}
