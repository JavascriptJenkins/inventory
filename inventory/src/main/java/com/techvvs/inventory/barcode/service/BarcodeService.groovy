package com.techvvs.inventory.barcode.service

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
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


    @Autowired
    BarcodeHelper barcodeHelper


    /* This method will create a single barcode for each product.
    *  If you want to make barcodes for every single product and multiples for the amount of products you have, use the other method
    *  */
    void createSingleMenuBarcodesForBatch(BatchVO batchVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
       try {

           LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)
           List<Set<ProductVO>> result = barcodeHelper.removeItemsInChunksOf50(linkedHashSet);

           System.out.println("result of rounding up: " + result);

            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodes(batchVO.name, batchVO.batchnumber, i, result.get(i));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    void createAllBarcodesForBatch(BatchVO batchVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)

            List<ProductVO> expandedList =expandAndDuplicateProductQuantities(linkedHashSet)

         //  LinkedHashSet expandedHashSet = barcodeHelper.convertListToLinkedHashSet(expandedList)

            // todo: for some reason the removeItemsInChunksOf50ReturnList isn't working properly
            // This needs to be a list of lists
            List<List<ProductVO>> result = barcodeHelper.removeItemsInChunksOf50ReturnList(expandedList);

            System.out.println("result of rounding up: " + result);

            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodesForAllItems(batchVO.name, batchVO.batchnumber, i, result.get(i));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<ProductVO> expandAndDuplicateProductQuantities(LinkedHashSet<ProductVO> originalSet) {
        List<ProductVO> expandedList = new ArrayList<>();

        for (ProductVO product : originalSet) {
            for (int i = 0; i < product.getQuantity(); i++) {
                expandedList.add(product) // for each quantity add the product again to the list
            }
        }

        return expandedList;
    }


}
