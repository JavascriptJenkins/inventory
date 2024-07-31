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


    void createAllQrsForBatch(BatchVO batchVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)

            List<ProductVO> expandedList =expandAndDuplicateProductQuantities(linkedHashSet)

            List<ProductVO> sortedlist = sortProductsByIdDescending(expandedList)

            List<List<ProductVO>> result1 = barcodeHelper.removeItemsInChunksOf50ReturnList(sortedlist);

            List<List<ProductVO>> result = reverseOrder(result1)

            System.out.println("result of rounding up: " + result);

            for(int i = 0; i < result.size(); i++) {
                qrCodeGenerator.generateQrcodesForAllItems(batchVO.name, batchVO.batchnumber, i, result.get(i), batchVO.name);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<ProductVO> convertSetToList(Set<ProductVO> productSet) {
        return new ArrayList<>(productSet);
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

    public static List<ProductVO> sortProductsByIdAscending(List<ProductVO> products) {
        Collections.sort(products, new Comparator<ProductVO>() {
            @Override
            public int compare(ProductVO p1, ProductVO p2) {
                return Integer.compare(p1.getProduct_id(), p2.getProduct_id());
            }
        });
        return products;
    }

    static List<List> reverseOrder(List<List> listOfLists) {
        listOfLists.reverse()
    }

    static List<ProductVO> sortProductsByIdDescending(List<ProductVO> products) {
        products.sort { a, b -> b.product_id <=> a.product_id }
        return products
    }
}