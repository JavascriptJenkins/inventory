package com.techvvs.inventory.barcode.service

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
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




    /* This method will create a single barcode for each product.
    *  If you want to make barcodes for every single product and multiples for the amount of products you have, use the other method
    *  */
    void createSingleMenuBarcodesForBatch(BatchVO batchVO) {


        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
       try {

           // get the number of products in the batch
           int numberOfProducts = batchVO.product_set.size();


           LinkedHashSet linkedHashSet = convertToLinkedHashSet(batchVO.product_set)
           List<Set<ProductVO>> result = removeItemsInChunksOf50(linkedHashSet);


           System.out.println("result of rounding up: " + result);

            // todo: verify this is running the correct number of times and the 0 index thing isn't messing it up


            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodes(batchVO.name, batchVO.batchnumber, i, result.get(i));
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public static int divideAndRoundUp(int num) {
        double result = num / 50.0; // Ensure the division is performed in floating-point.
        return (int) Math.ceil(result); // Use Math.ceil to round up and cast to int.
    }



    public static <T> List<Set<T>> removeItemsInChunksOf50(LinkedHashSet<T> originalSet) {
        List<Set<T>> chunks = new ArrayList<>();
        Iterator<T> iterator = originalSet.iterator();

        while (iterator.hasNext()) {
            Set<T> chunk = new LinkedHashSet<>();
            int count = 0;
            while (iterator.hasNext() && count < 50) {
                T item = iterator.next();
                chunk.add(item);
                iterator.remove();  // Remove the item from the original set
                count++;
            }
            chunks.add(chunk);
        }

        return chunks;
    }


    public static <T> LinkedHashSet<T> convertToLinkedHashSet(Set<T> originalSet) {
        return new LinkedHashSet<>(originalSet);
    }



}
