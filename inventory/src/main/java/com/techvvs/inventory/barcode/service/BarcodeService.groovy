package com.techvvs.inventory.barcode.service

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


//UPCA format
//0: Number system character
//12345: Manufacturer code
//67890: Product code
//5: Check digit
//https://www.gs1us.org/upcs-barcodes-prefixes/how-to-get-a-upc-barcode
@Component
class BarcodeService {

    @Autowired
    BarcodeGenerator barcodeGenerator;


    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    AppConstants appConstants

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

            List<ProductVO> sortedlist = sortProductsByIdDescending(expandedList)

            List<List<ProductVO>> result1 = barcodeHelper.removeItemsInChunksOf50ReturnList(sortedlist);

            List<List<ProductVO>> result3 = reverseOrder(result1)
            List<List<ProductVO>> result = sortProductListsByName(result3)

            System.out.println("result of rounding up: " + result);

            // todo: make pass in the parent pdf file so all pages go into individual file
            // create document before we loop over the collections of products so all pdf pages land in a single document
            PDDocument document = new PDDocument()
            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodesForAllItems(batchVO.name, batchVO.batchnumber, i, result.get(i), batchVO.name, document);
            }
            String filename = batchVO.name+"-"+batchVO.batchnumber

            // save the actual file after looping thru all products
            document.save(appConstants.PARENT_LEVEL_DIR+batchVO.batchnumber+appConstants.BARCODES_ALL_DIR+appConstants.filenameprefix+filename+".pdf");
            document.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static List<List<ProductVO>> sortProductListsByName(List<List<ProductVO>> result) {
        for (List<ProductVO> productList : result) {
            Collections.sort(productList, new Comparator<ProductVO>() {
                @Override
                public int compare(ProductVO p1, ProductVO p2) {
                    return p1.getName().compareToIgnoreCase(p2.getName());
                }
            });
        }
        return result;
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

    static List<ProductVO> sortProductsByIdDescending(List<ProductVO> products) {
        products.sort { a, b -> b.product_id <=> a.product_id }
        return products
    }

    static List<List> reverseOrder(List<List> listOfLists) {
        listOfLists.reverse()
    }


}
