package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.security.SecureRandom

@Component
class ProductHelper {

    @Autowired
    ProductRepo productRepo

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    BarcodeService barcodeService

    SecureRandom secureRandom = new SecureRandom();

    ProductVO loadProduct(int productid){
        Optional<ProductVO> productVO = productRepo.findById(productid)
        return productVO.get()
    }


    List<List<ProductVO>> sortAndExpandProductList(List<ProductVO> product_list){
        List<ProductVO> sortedlist = barcodeService.sortProductsByIdDescending(product_list)
        List<List<ProductVO>> result1 = barcodeHelper.removeItemsInChunksOf50ReturnList(sortedlist);
        List<List<ProductVO>> result3 = barcodeService.reverseOrder(result1)
        List<List<ProductVO>> result = barcodeService.sortProductListsByName(result3)
        return result
    }


    List<List<ProductVO>> sortAndExpandProductSet(Set<ProductVO> product_set){
        LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(product_set)

        // setting this to 100 ensures that only 1 page maximum of barcodes will be generated for each SKU
        // This is fine - if people need more than 50 barcodes for their individual products, they can print more pdfs!
        List<ProductVO> expandedList = barcodeService.expandAndDuplicateProductQuantitiesWithLimit(linkedHashSet, 50)

        List<ProductVO> sortedlistLowToHighPrice = barcodeService.sortByPrice(expandedList)

        List<List<ProductVO>> result1 = barcodeHelper.splitIntoChunksOf50(sortedlistLowToHighPrice);

        return result1
    }


    String generateProductNumber() {

        int productNumber = generateIntProductNumber()

        // Loop until a unique batch number is generated
        productNumber = generateIntProductNumber()
        while (productRepo.existsByProductnumber(productNumber)) {
            productNumber = generateIntProductNumber()
        }



        return productNumber.toString(); // cast to a string so it can be inserted in excel cells
    }

    int generateIntProductNumber(){
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


    PackageVO consolidateProductListForDisplay(PackageVO packageVO){
        packageVO.displayquantitytotal = 0
        packageVO.product_package_list.sort { a, b -> a.price <=> b.price }
        Map<Integer, ProductVO> productMap = new HashMap<>();
        // cycle thru here and if the productid is the same then update the quantity
        for(ProductVO productinpackage : packageVO.product_package_list){

            if(productinpackage.displayquantity == null){
                productinpackage.displayquantity = 1
            } else {
                productinpackage.displayquantity = productinpackage.displayquantity + 1
            }

            productMap.put(productinpackage.getProduct_id(), productinpackage)
        }
        packageVO.product_package_list = new ArrayList<>(productMap.values());
//        for(ProductVO productinpackage : packageVO.product_package_list) {
//            productinpackage.displayquantity += productinpackage.displayquantity
//        }
        return packageVO

    }




}
