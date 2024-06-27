package com.techvvs.inventory.barcode.service

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.model.BatchVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


// todo: enhancements
/*
** Add functionality to let barcodes be printed for specific product types
* */

@Component
class BarcodeService {

    @Autowired
    BarcodeGenerator barcodeGenerator;




    /* This method will create a single barcode for each product.
    *  If you want to make barcodes for every single product and multiples for the amount of products you have, use the other method
    *  */
    void createSingleMenuBarcodesForBatch(BatchVO batchVO) {


        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
       try {

           // get the number of products in the batch
           int numberOfProducts = batchVO.product_set.size();



            // todo: verify this is running the correct number of times and the 0 index thing isn't messing it up
            // run it 8 times 8*50 = 400
            for(int i = 0; i < numberOfProducts; i++) {
                barcodeGenerator.generateBarcodes(batchVO.name+"-"+batchVO.batchnumber);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }





}
