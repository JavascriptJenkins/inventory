package com.techvvs.inventory.qrcode

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.qrcode.impl.QrCodeGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class QrCodeService {

    @Autowired
    QrCodeGenerator qrCodeGenerator

    @Autowired
    BarcodeHelper barcodeHelper


    /* This method will create a single barcode for each product.
*  If you want to make barcodes for every single product and multiples for the amount of products you have, use the other method
*  */
    void createSingleMenuQrsForBatch(BatchVO batchVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)
            List<Set<ProductVO>> result = barcodeHelper.removeItemsInChunksOf50(linkedHashSet);

            System.out.println("result of rounding up: " + result);

            for(int i = 0; i < result.size(); i++) {
                //qrCodeGenerator.generateBarcodes(batchVO.name, batchVO.batchnumber, i, result.get(i));

                qrCodeGenerator.generateQrcodes(batchVO.name, batchVO.batchnumber, i, result.get(i));

                //qrCodeGenerator.generateQRCodes(convertSetToList(batchVO.getProduct_set()))
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<ProductVO> convertSetToList(Set<ProductVO> productSet) {
        return new ArrayList<>(productSet);
    }

}
